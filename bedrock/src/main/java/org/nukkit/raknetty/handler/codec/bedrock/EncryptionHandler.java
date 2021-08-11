package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.nukkit.raknetty.channel.nio.BedrockChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EncryptionHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionHandler.class);

    private BedrockChannel channel;

    public EncryptionHandler(BedrockChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte packetId = buf.readByte();

        if (packetId == (byte) 0xfe) {
            if (channel.isEncrypted()) {
                // TODO: try to decrypt
                //  get shared secret
                //  check MAC
            }

            // pass the packet to inflater
            ctx.fireChannelRead(buf);
        } else {
            LOGGER.debug("Unhandled: {}", ByteBufUtil.prettyHexDump(buf));
            ReferenceCountUtil.release(buf);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (channel.isEncrypted()) {
            // TODO: do encryption
        }
        ctx.write(msg, promise);
    }
}
