package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.BinaryUtil;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class ConnectionRequestAccepted implements ReliabilityMessage {

    public InetSocketAddress clientAddress;
    public int systemIndex = 0;
    public InetSocketAddress[] ipList;
    public long requestTime;
    public long replyTime;

    public ConnectionRequestAccepted(int numberOfInternalIds) {
        ipList = new InetSocketAddress[numberOfInternalIds];
        Arrays.fill(ipList, new InetSocketAddress(0));
    }

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_CONNECTION_REQUEST_ACCEPTED;
    }

    @Override
    public void encode(ByteBuf buf) {
        BinaryUtil.writeAddress(buf, clientAddress);
        buf.writeShort(systemIndex);
        for (int i = 0; i < ipList.length; i++) {
            BinaryUtil.writeAddress(buf, ipList[i]);
        }
        buf.writeLong(requestTime);
        buf.writeLong(replyTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        clientAddress = BinaryUtil.readAddress(buf);
        systemIndex = buf.readShort();
        for (int i = 0; i < ipList.length; i++) {
            if (buf.readableBytes() <= 16) break;
            ipList[i] = BinaryUtil.readAddress(buf);
        }
        requestTime = buf.readLong();
        replyTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientAddress", clientAddress)
                .append("systemIndex", systemIndex)
                .append("ipList", ipList)
                .append("requestTime", requestTime)
                .append("replyTime", replyTime)
                .toString();
    }
}
