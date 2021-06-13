package org.nukkit.raknetty.handler.codec.offline;

import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;

public class DefaultOfflineHandler extends AbstractOfflineHandler {
    @Override
    public void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage packet, InetSocketAddress sender) {
        //TODO: client-side offline packet handling
    }
}
