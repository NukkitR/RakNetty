package org.nukkit.raknetty.handler.codec.offline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;

public class DefaultOfflineHandler extends AbstractOfflineHandler {
    public DefaultOfflineHandler(Channel channel) {
        super(channel);
    }

    @Override
    public void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage packet, InetSocketAddress sender) {
        //TODO: client-side offline packet handling
    }
}
