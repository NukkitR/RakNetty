package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginException;
import org.nukkit.raknetty.handler.codec.bedrock.packet.InventorySlotPacket;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class NetworkHandlerAdapter extends ChannelInboundHandlerAdapter implements NetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkHandlerAdapter.class);

    private final Map<
            Class<? extends BedrockPacket>,
            UnsafeConsumer<? extends BedrockPacket>> dispatcher = new HashMap<>();

    LoginStatus loginStatus = LoginStatus.NO_ACTION;
    final LoginFuture loginFuture;
    final BedrockChannel channel;

    public NetworkHandlerAdapter(BedrockChannel channel) {
        this.channel = channel;
        loginFuture = new LoginFuture(channel);

        registerPacketEvent(InventorySlotPacket.class, this::handle);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, InventorySlotPacket in) throws Exception {
        LOGGER.debug("{}", in);
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
            packet = new BedrockPacketHolder();
            ((BedrockPacketHolder) packet).id = packetId;
        }
        packet.decode(buf);

        dispatch(ctx, packet);
        ReferenceCountUtil.release(msg);
    }

    protected final <P extends BedrockPacket> void registerPacketEvent(
            Class<P> packetClass,
            UnsafeConsumer<P> consumer) {
        this.dispatcher.put(packetClass, consumer);
    }

    @SuppressWarnings("unchecked")
    private <P extends BedrockPacket> void dispatch(ChannelHandlerContext ctx, P in) throws Exception {
        UnsafeConsumer<P> consumer =
                (UnsafeConsumer<P>) this.dispatcher.get(in.getClass());
        if (consumer == null) {
            ctx.fireChannelRead(in);
        } else {
            consumer.accept(ctx, in);
        }
    }

    @Override
    public ChannelFuture loginFuture() {
        return this.loginFuture;
    }

    @FunctionalInterface
    protected interface UnsafeConsumer<P extends BedrockPacket> {
        void accept(ChannelHandlerContext ctx, P packet) throws Exception;
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
