package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class BatchPacketHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchPacketHandler.class);

    private final BedrockChannel channel;
    private ChannelHandlerContext ctx;
    private final LinkedList<BedrockPacket> sendQueue = new LinkedList<>();
    private Future<?> updateTask;

    public BatchPacketHandler(BedrockChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;

            while (buf.isReadable()) {
                int size = (int) VarIntUtil.readUnsignedVarInt(buf);
                ByteBuf slice = buf.slice(buf.readerIndex(), size);
                buf.skipBytes(size);
                ctx.fireChannelRead(slice.retain());
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        BedrockPacket packet = (BedrockPacket) msg;

        switch (packet.getId()) {
            case LOGIN:
            case CLIENT_TO_SERVER_HANDSHAKE:
            case SERVER_TO_CLIENT_HANDSHAKE:
            case DISCONNECT:
            case PLAY_STATUS:
                // send directly for login procedure packets
                doWrite(ctx, packet);
                break;
            default:
                sendQueue.add(packet);
        }
    }

    @Override
    public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (channel.loginFuture().isSuccess()) {
            // send all packets in the queue before disconnecting if we have already logged in.
            doWrite(ctx, sendQueue);
        }
        sendQueue.clear();
        super.disconnect(ctx, promise);
    }

    private void doWrite(ChannelHandlerContext ctx, BedrockPacket... packets) {
        doWrite(ctx, Arrays.asList(packets));
    }

    private void doWrite(ChannelHandlerContext ctx, Collection<BedrockPacket> packets) {
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
            if (!channel.loginFuture().isSuccess()) return;
            if (ctx == null) return;
            doWrite(ctx, sendQueue);
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
}
