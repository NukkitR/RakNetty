package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

import java.net.InetSocketAddress;

public class ConnectionRequestAccepted implements ReliabilityMessage {

    public InetSocketAddress clientAddress;
    public int systemIndex = 0;
    public InetSocketAddress[] ipList = new InetSocketAddress[MAXIMUM_NUMBER_OF_INTERNAL_IDS];
    public long requestTime;
    public long replyTime;

    public ConnectionRequestAccepted() {
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
            ipList[i] = UNASSIGNED_SYSTEM_ADDRESS;
        }
    }

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_CONNECTION_REQUEST_ACCEPTED);
        PacketUtil.writeAddress(buf, clientAddress);
        buf.writeShort(systemIndex);
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
            PacketUtil.writeAddress(buf, ipList[i]);
        }
        buf.writeLong(requestTime);
        buf.writeLong(replyTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        clientAddress = PacketUtil.readAddress(buf);
        systemIndex = buf.readShort();
        for (int i = 0; i < MAXIMUM_NUMBER_OF_INTERNAL_IDS; i++) {
            ipList[i] = PacketUtil.readAddress(buf);
        }
        requestTime = buf.readLong();
        replyTime = buf.readLong();
    }
}
