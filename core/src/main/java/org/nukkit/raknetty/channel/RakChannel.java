package org.nukkit.raknetty.channel;


import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;
import org.nukkit.raknetty.handler.codec.reliability.SlidingWindow;

import java.net.InetSocketAddress;


public interface RakChannel extends Channel {

    long localGuid();

    long remoteGuid();

    int mtuSize();

    ConnectMode connectMode();

    RakChannel connectMode(ConnectMode mode);

    SlidingWindow slidingWindow();

    @Override
    RakChannelConfig config();

    @Override
    InetSocketAddress localAddress();

    @Override
    InetSocketAddress remoteAddress();

    DatagramChannel udpChannel();

    enum ConnectMode {
        NO_ACTION,
        DISCONNECT_ASAP,
        DISCONNECT_ASAP_SILENTLY,
        DISCONNECT_ON_NO_ACK,
        REQUESTED_CONNECTION,
        HANDLING_CONNECTION_REQUEST,
        UNVERIFIED_SENDER,
        CONNECTED
    }

}
