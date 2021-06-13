package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ServerChannel;
import io.netty.channel.socket.DatagramChannel;
import org.nukkit.raknetty.handler.codec.offline.OpenConnectionRequest2;

import java.net.InetSocketAddress;


public interface RakServerChannel extends ServerChannel {

    void accept(ChannelHandlerContext ctx, OpenConnectionRequest2 request, InetSocketAddress remoteAddress);

    boolean allowNewConnections();

    RakChannel getChannel(InetSocketAddress address);

    RakChannel getChannel(long guid);

    long localGuid();

    long remoteGuid();

    @Override
    RakServerChannelConfig config();

    @Override
    InetSocketAddress localAddress();

    @Override
    InetSocketAddress remoteAddress();

    DatagramChannel udpChannel();
}
