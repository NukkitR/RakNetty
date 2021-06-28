package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

import java.net.InetSocketAddress;

public class NewIncomingConnection implements ReliabilityMessage {

    public InetSocketAddress serverAddress;
    public InetSocketAddress[] clientAddresses = new InetSocketAddress[MAXIMUM_NUMBER_OF_INTERNAL_IDS];
    public long pingTime;
    public long pongTime;

    public NewIncomingConnection() {
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
            clientAddresses[i] = UNASSIGNED_SYSTEM_ADDRESS;
        }
    }

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_NEW_INCOMING_CONNECTION);
        PacketUtil.writeAddress(buf, serverAddress);
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
            PacketUtil.writeAddress(buf, clientAddresses[i]);
        }
        buf.writeLong(pingTime);
        buf.writeLong(pongTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        serverAddress = PacketUtil.readAddress(buf);
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
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
