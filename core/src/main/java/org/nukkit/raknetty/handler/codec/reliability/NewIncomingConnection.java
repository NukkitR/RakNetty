package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.BinaryUtil;

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
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_NEW_INCOMING_CONNECTION;
    }

    @Override
    public void encode(ByteBuf buf) {
        BinaryUtil.writeAddress(buf, serverAddress);
        for (int i = 0; i < clientAddresses.length; i++) {
            BinaryUtil.writeAddress(buf, clientAddresses[i]);
        }
        buf.writeLong(pingTime);
        buf.writeLong(pongTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        serverAddress = BinaryUtil.readAddress(buf);
        for (int i = 0; i < clientAddresses.length; i++) {
            clientAddresses[i] = BinaryUtil.readAddress(buf);
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
