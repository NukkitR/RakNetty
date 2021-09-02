package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.RakNetUtil;

import java.net.InetSocketAddress;

public class OpenConnectionRequest2 implements OfflineMessage {

    public InetSocketAddress serverAddress;
    public int mtuSize;
    public long clientGuid;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_OPEN_CONNECTION_REQUEST_2;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        RakNetUtil.writeAddress(buf, serverAddress);
        buf.writeShort(mtuSize);
        buf.writeLong(clientGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        serverAddress = RakNetUtil.readAddress(buf);
        mtuSize = buf.readShort();
        clientGuid = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverAddress", serverAddress)
                .append("mtuSize", mtuSize)
                .append("clientGuid", clientGuid)
                .toString();
    }
}
