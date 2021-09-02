package org.nukkit.raknetty.handler.codec.bedrock;

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginException;
import org.nukkit.raknetty.channel.bedrock.LoginStatus;
import org.nukkit.raknetty.handler.codec.bedrock.packet.BedrockPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.function.Consumer;

public abstract class AbstractNetworkHandler extends ChannelInboundHandlerAdapter implements NetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNetworkHandler.class);

    protected LoginStatus loginStatus = LoginStatus.LOGIN;
    protected final LoginFuture loginFuture;
    protected final BedrockChannel channel;
    protected ChannelHandlerContext ctx;

    private final Map<Class<? extends BedrockPacket>, Consumer<? extends BedrockPacket>> consumerMap = Maps.newHashMap();

    public AbstractNetworkHandler(BedrockChannel channel) {
        this.channel = channel;
        loginFuture = new LoginFuture(channel);
        registerPackets();
    }

    protected abstract void registerPackets();

    protected final <T extends BedrockPacket> void registerPacket(Class<T> clazz, Consumer<T> consumer) {
        Consumer<?> existing = consumerMap.put(clazz, consumer);
        if (existing != null) {
            throw new IllegalStateException("Duplicate packet handler for " + clazz);
        }
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        BedrockByteBuf buf = BedrockByteBuf.wrap((ByteBuf) msg);

        int pid = buf.readUnsignedVarInt() & 0x3ff;
        PacketIdentifier packetId = PacketIdentifier.valueOf(pid);

        if (packetId == null) {
            // TODO: disconnect client when fails
            LOGGER.debug("Unknown pid: {}", pid);
            LOGGER.debug("Content: {}", ByteBufUtil.prettyHexDump(buf));
            return;
        }

        BedrockPacket packet = packetId.createPacket();

        if (packet == null) {
            BedrockPacketHolder holder = new BedrockPacketHolder();
            holder.decode(buf);
            holder.id = packetId;
            ctx.fireChannelRead(holder);

        } else {
            packet.decode(BedrockByteBuf.wrap(buf));
            dispatch(packet);
        }

        ReferenceCountUtil.release(msg);
    }

    @SuppressWarnings("unchecked")
    private <P extends BedrockPacket> void dispatch(P in) throws Exception {
        if (!in.getId().satisfies(loginStatus)) {
            LOGGER.warn("Unhandled: {}", in);
        }

        Consumer<P> consumer = (Consumer<P>) this.consumerMap.get(in.getClass());
        if (consumer == null) {
            ctx.fireChannelRead(in);
        } else {
            consumer.accept(in);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.ctx = ctx;
    }

    @Override
    public ChannelFuture loginFuture() {
        return this.loginFuture;
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
