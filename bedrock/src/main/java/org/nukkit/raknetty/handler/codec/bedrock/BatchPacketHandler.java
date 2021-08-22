package org.nukkit.raknetty.handler.codec.bedrock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginException;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;
import org.nukkit.raknetty.handler.codec.bedrock.packet.PlayStatusPacket.PlayStatus;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BatchPacketHandler extends ChannelDuplexHandler {

    enum LoginStatus {
        NO_ACTION,
        HANDSHAKING,            // client send LoginPacket OR server receive LoginPacket and send ServerToClientHandshakePacket
        SUCCESS,              // client send ClientToServerHandshakePacket OR server receive ClientToServerHandshakePacket
        FAILED
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchPacketHandler.class);

    private final BedrockChannel channel;
    private ChannelHandlerContext ctx;
    private final LinkedList<BedrockPacket> sendQueue = new LinkedList<>();
    private Future<?> updateTask;
    private LoginStatus loginStatus = LoginStatus.NO_ACTION;
    private final LoginFuture loginFuture;

    public BatchPacketHandler(BedrockChannel channel) {
        this.channel = channel;
        loginFuture = new LoginFuture(channel);
    }

    public ChannelFuture loginFuture() {
        return this.loginFuture;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;

            while (buf.isReadable()) {
                int size = (int) VarIntUtil.readUnsignedVarInt(buf);
                ByteBuf slice = buf.slice(buf.readerIndex(), size);
                buf.skipBytes(size);

                int pid = (int) (VarIntUtil.readUnsignedVarInt(slice) & 0x3ff);
                PacketIdentifier packetId = PacketIdentifier.valueOf(pid);

                if (packetId == null) {
                    // TODO: disconnect client when fails
                    LOGGER.debug("Unknown pid: {}", buf.getUnsignedByte(buf.readerIndex()));
                    LOGGER.debug("Content: {}", ByteBufUtil.prettyHexDump(slice));
                    continue;
                }

                switch (packetId) {
                    case LOGIN:
                        handleLoginPacket(ctx, slice);
                        continue;
                    case SERVER_TO_CLIENT_HANDSHAKE:
                        handleServerToClientHandshake(ctx, slice);
                        continue;
                    case CLIENT_TO_SERVER_HANDSHAKE:
                        handleClientToServerHandshake(ctx, slice);
                        continue;
                    case PLAY_STATUS:
                        handlePlayStatusPacket(ctx, slice);
                        continue;
                    case DISCONNECT:
                        handleDisconnectPacket(ctx, slice);
                        continue;
                    default:
                        // NOOP
                }

                if (loginStatus == LoginStatus.SUCCESS) {
                    // TODO: replace with factory method
                    BedrockPacketAdapter packet = new BedrockPacketAdapter();
                    packet.id = packetId;
                    packet.decode(slice);

                    ctx.fireChannelRead(packet);
                } else {
                    // receive general packets before the handshake is done.
                    LOGGER.debug("Closing the channel {} due to unfinished handshaking.", channel);
                    LOGGER.debug(ByteBufUtil.prettyHexDump(slice));
                    channel.close();
                }
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    private void handleLoginPacket(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        // the packet must be sent from client to server
        if (channel.isClient() || loginStatus != LoginStatus.NO_ACTION) {
            return;
        }

        LoginPacket packet = new LoginPacket();
        packet.decode(buf);
        Jws<Claims> jws;
        if (channel.config().isOnlineAuthenticationEnabled()) {
            //jwt = WebTokenUtil.verifyOnline(packet.tokens);
            jws = WebTokenUtil.verifyOnline(packet.tokens);
        } else {
            //jwt = WebTokenUtil.verifySelfSigned(packet.tokens);
            jws = WebTokenUtil.verifySelfSigned(packet.tokens);
        }

        // TODO: disconnect() if not authenticated.

        ECPublicKey clientPublicKey = WebTokenUtil.readECPublicKey(
                jws.getBody().get("identityPublicKey", String.class));

        LOGGER.debug("Login");
        //LOGGER.debug("Protocol: {}", packet.protocolVersion);
        //LOGGER.debug("Client key: {}", clientPublicKey);
        //LOGGER.debug("Extra data: {}", jwt.getClaim("extraData").asMap());
        //LOGGER.debug("Skin data: {}", packet.skinJwt);

        byte[] salt = new byte[16];
        ThreadLocalRandom.current().nextBytes(salt);

        ServerToClientHandshake out = new ServerToClientHandshake();
        out.serverPrivateKey = (ECPrivateKey) channel.localPrivateKey();
        out.serverPublicKey = (ECPublicKey) channel.localPublicKey();
        out.salt = salt;
        doSend(ctx, out);

        // enable the encryption after the handshake is sent, so that it would not be encrypted.
        channel.enableEncryption(clientPublicKey, salt);
        loginStatus = LoginStatus.HANDSHAKING;
    }

    private void handleServerToClientHandshake(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (!channel.isClient() || loginStatus != LoginStatus.NO_ACTION) return;

        ServerToClientHandshake in = new ServerToClientHandshake();
        in.decode(buf);
        LOGGER.debug("S->C Handshake");
        //LOGGER.debug("Server Public Key: {}", packet.serverPublicKey);
        LOGGER.debug("Salt: {}", Base64.getEncoder().encodeToString(in.salt));

        // enable the encryption before the handshake so that the server will ensure the encryption is successfully established.
        channel.enableEncryption(in.serverPublicKey, in.salt);
        ClientToServerHandshake out = new ClientToServerHandshake();
        doSend(ctx, out);

        loginStatus = LoginStatus.HANDSHAKING;
    }

    private void handleClientToServerHandshake(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (channel.isClient() || loginStatus != LoginStatus.HANDSHAKING) return;

        loginStatus = LoginStatus.SUCCESS;
        loginFuture.loginSuccess();
        LOGGER.debug("C->S Handshake");

        PlayStatusPacket out = new PlayStatusPacket();
        out.status = PlayStatus.LOGIN_SUCCESS;
        doSend(ctx, out);
    }

    private void handlePlayStatusPacket(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (!channel.isClient()) return;

        PlayStatusPacket in = new PlayStatusPacket();
        in.decode(buf.duplicate());

        PlayStatus status = in.status;
        LOGGER.debug("Play status: {}", status);
        if (status == PlayStatus.LOGIN_SUCCESS && loginStatus == LoginStatus.HANDSHAKING) {
            loginStatus = LoginStatus.SUCCESS;
            loginFuture.loginSuccess();

        } else if (status.isSuccess()) {
            // could be PLAYER_SPAWN
            ctx.fireChannelRead(buf);

        } else {
            // login is not success
            loginStatus = LoginStatus.FAILED;
            loginFuture.loginFailure(status.toString());
            channel.disconnect();
        }
    }

    private void handleDisconnectPacket(ChannelHandlerContext ctx, ByteBuf buf) throws Exception {
        if (!channel.isClient()) return;

        DisconnectPacket in = new DisconnectPacket();
        in.decode(buf);

        LOGGER.debug("Disconnected from server: {}", in.reason);
        channel.disconnect();
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        BedrockPacket packet = (BedrockPacket) msg;
        sendQueue.add(packet);
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (loginStatus == LoginStatus.SUCCESS) {
            doSend(ctx, sendQueue); // send all packets before disconnecting
            sendQueue.clear();
        }

        super.disconnect(ctx, promise);
    }

    public void doSend(ChannelHandlerContext ctx, BedrockPacket... packets) {
        doSend(ctx, Arrays.asList(packets));
    }

    public void doSend(ChannelHandlerContext ctx, Collection<BedrockPacket> packets) {
        CompositeByteBuf out = ctx.alloc().compositeBuffer();

        for (BedrockPacket packet : packets) {
            try {
                ByteBuf body = ctx.alloc().buffer();
                VarIntUtil.writeUnsignedVarInt(body, packet.getId().ordinal());
                packet.encode(body);

                ByteBuf header = ctx.alloc().buffer(5, 10);
                VarIntUtil.writeUnsignedVarInt(header, body.readableBytes());
                out.addComponents(true, header, body);
            } catch (Exception e) {
                LOGGER.debug("Skipping one packet: {}", packet, e);
            }
        }

        ctx.write(out);
    }

    private void update() {
        try {
            if (loginStatus != LoginStatus.SUCCESS) return;
            if (ctx == null) return;
            doSend(ctx, sendQueue);
            sendQueue.clear();

        } finally {
            if (channel.isActive()) {
                updateTask = channel.eventLoop().schedule(this::update, 1000 / 20, TimeUnit.MILLISECONDS);
            }
        }
    }

    public ChannelHandlerContext ctx() {
        return this.ctx;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Validate.isTrue(this.ctx == null, "handler has already been added to a pipeline.");
        this.ctx = ctx;
        super.handlerAdded(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (updateTask == null) {
            updateTask = ctx.channel().eventLoop().submit(this::update);
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
        super.channelInactive(ctx);
    }

    static class LoginFuture extends DefaultChannelPromise {
        LoginFuture(BedrockChannel channel) {
            super(channel);
        }

        @Override
        public ChannelPromise setSuccess() {
            throw new IllegalStateException();
        }

        @Override
        public ChannelPromise setFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        @Override
        public boolean trySuccess() {
            throw new IllegalStateException();
        }

        @Override
        public boolean tryFailure(Throwable cause) {
            throw new IllegalStateException();
        }

        boolean loginSuccess() {
            return super.trySuccess();
        }

        boolean loginFailure(String msg) {
            return super.tryFailure(new LoginException(msg));
        }
    }
}
