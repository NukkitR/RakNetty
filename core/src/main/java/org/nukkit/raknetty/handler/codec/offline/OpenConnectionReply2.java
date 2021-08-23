package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.BinaryUtil;

import java.net.InetSocketAddress;

public class OpenConnectionReply2 implements OfflineMessage {

    public long serverGuid;
    public InetSocketAddress clientAddress;
    public int mtuSize;
    public final boolean hasSecurity = false;   // TODO: implement security

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_OPEN_CONNECTION_REPLY_2;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(serverGuid);
        BinaryUtil.writeAddress(buf, clientAddress);
        buf.writeShort(mtuSize);
        buf.writeBoolean(hasSecurity);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        serverGuid = buf.readLong();
        clientAddress = BinaryUtil.readAddress(buf);
        mtuSize = buf.readShort();
        buf.skipBytes(1);                      // TODO: implement security
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverGuid", serverGuid)
                .append("clientAddress", clientAddress)
                .append("mtuSize", mtuSize)
                .append("hasSecurity", hasSecurity)
                .toString();
    }
}
