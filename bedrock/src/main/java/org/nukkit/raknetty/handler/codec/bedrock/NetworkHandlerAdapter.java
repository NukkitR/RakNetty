package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginException;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkHandlerAdapter extends ChannelInboundHandlerAdapter implements NetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandlerAdapter.class);

    LoginStatus loginStatus = LoginStatus.NO_ACTION;
    final LoginFuture loginFuture;
    final BedrockChannel channel;

    public NetworkHandlerAdapter(BedrockChannel channel) {
        this.channel = channel;
        loginFuture = new LoginFuture(channel);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf buf = (ByteBuf) msg;

        int pid = (int) (VarIntUtil.readUnsignedVarInt(buf) & 0x3ff);
        PacketIdentifier packetId = PacketIdentifier.valueOf(pid);

        if (packetId == null) {
            // TODO: disconnect client when fails
            LOGGER.debug("Unknown pid: {}", pid);
            LOGGER.debug("Content: {}", ByteBufUtil.prettyHexDump(buf));
            return;
        }

        BedrockPacket packet = packetId.createPacket();

        if (packet == null) {
            packet = new BedrockPacketAdapter();
            ((BedrockPacketAdapter) packet).id = packetId;
        }
        packet.decode(buf);

        dispatch(ctx, packet);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public ChannelFuture loginFuture() {
        return this.loginFuture;
    }

    public void dispatch(ChannelHandlerContext ctx, BedrockPacket in) throws Exception {
        // TODO:
        //  handle clientbound and serverbound
        ctx.fireChannelRead(in);
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

        boolean markAsSuccess() {
            return super.trySuccess();
        }

        boolean markAsFailed(String msg) {
            return super.tryFailure(new LoginException(msg));
        }
    }
}
