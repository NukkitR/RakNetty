package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.ByteUtil;

import java.net.InetSocketAddress;

public class OpenConnectionRequest2 implements OfflineMessage {

    public InetSocketAddress serverAddress;
    public int mtuSize;
    public long clientGuid;

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_OPEN_CONNECTION_REQUEST_2);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        ByteUtil.writeAddress(buf, serverAddress);
        buf.writeShort(mtuSize);
        buf.writeLong(clientGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        serverAddress = ByteUtil.readAddress(buf);
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
