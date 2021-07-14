package org.nukkit.raknetty.handler.codec.offline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.channel.AddressedMessage;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.handler.codec.Message;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;

public class DefaultClientOfflineHandler extends AbstractOfflineHandler {
    public DefaultClientOfflineHandler(Channel channel) {
        super(channel);
    }

    @Override
    public void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage msg, InetSocketAddress sender) {
        Message reply = null;
        long now = System.nanoTime();

        try {
            if (msg instanceof UnconnectedPing) {

            }

            if (msg instanceof OpenConnectionRequest1) {

            }

            if (msg instanceof OpenConnectionRequest2) {

            }

        } finally {
            if (reply != null) {
                ctx.writeAndFlush(new AddressedMessage(reply, sender));
            }
        }
    }

}
