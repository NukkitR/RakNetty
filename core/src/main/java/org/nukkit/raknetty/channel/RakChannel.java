package org.nukkit.raknetty.channel;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.socket.DatagramChannel;
import org.nukkit.raknetty.channel.nio.NioRakChannel;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.handler.codec.reliability.SlidingWindow;

import java.net.InetSocketAddress;


public interface RakChannel extends Channel {

    boolean isClient();

    long localGuid();

    long remoteGuid();

    int mtuSize();

    NioRakChannel mtuSize(int mtuSize);

    ConnectMode connectMode();

    RakChannel connectMode(ConnectMode mode);

    @Override
    RakServerChannel parent();

    void ping(PacketReliability reliability);

    int averagePing();

    void send(ReliabilityMessage message, PacketPriority priority, PacketReliability reliability, int orderingChannel);

    void send(ByteBuf message, PacketPriority priority, PacketReliability reliability, int orderingChannel);

    SlidingWindow slidingWindow();

    @Override
    RakChannelConfig config();

    @Override
    InetSocketAddress localAddress();

    @Override
    InetSocketAddress remoteAddress();

    DatagramChannel udpChannel();

    enum ConnectMode {
        NO_ACTION(
                false, false),
        DISCONNECT_ASAP(
                true, false),
        DISCONNECT_ASAP_SILENTLY(
                true, false),
        DISCONNECT_ON_NO_ACK(
                true, false),
        REQUESTED_CONNECTION(
                false, true),
        HANDLING_CONNECTION_REQUEST(
                false, true),
        UNVERIFIED_SENDER(
                false, true),
        CONNECTED(
                true, true);

        private final boolean isConnected;
        private final boolean canDisconnect;

        ConnectMode(boolean isConnected, boolean canDisconnect) {
            this.isConnected = isConnected;
            this.canDisconnect = canDisconnect;
        }

        public boolean isConnected() {
            return isConnected;
        }

        public boolean canDisconnect() {
            return canDisconnect;
        }
    }

}
