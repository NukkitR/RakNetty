package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class NewIncomingConnection implements ReliabilityMessage {

    public InetSocketAddress serverAddress;
    public InetSocketAddress[] clientAddresses;
    public long pingTime;
    public long pongTime;

    public NewIncomingConnection(int numberOfInternalIds) {
        clientAddresses = new InetSocketAddress[numberOfInternalIds];
        Arrays.fill(clientAddresses, new InetSocketAddress(0));
    }

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_NEW_INCOMING_CONNECTION);
        PacketUtil.writeAddress(buf, serverAddress);
        for (int i = 0; i < clientAddresses.length; i++) {
            PacketUtil.writeAddress(buf, clientAddresses[i]);
        }
        buf.writeLong(pingTime);
        buf.writeLong(pongTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        serverAddress = PacketUtil.readAddress(buf);
        for (int i = 0; i < clientAddresses.length; i++) {
            clientAddresses[i] = PacketUtil.readAddress(buf);
        }
        pingTime = buf.readLong();
        pongTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverAddress", serverAddress)
                .append("clientAddresses", clientAddresses)
                .append("pingTime", pingTime)
                .append("pongTime", pongTime)
                .toString();
    }
}
