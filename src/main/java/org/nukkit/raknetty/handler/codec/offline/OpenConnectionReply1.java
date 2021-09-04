package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

public class OpenConnectionReply1 implements OfflineMessage {

    public long serverGuid;
    public final boolean hasSecurity = false;   // TODO: implement security
    public int mtuSize;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_OPEN_CONNECTION_REPLY_1;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(serverGuid);
        buf.writeBoolean(hasSecurity);
        buf.writeShort(mtuSize);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        serverGuid = buf.readLong();
        buf.skipBytes(1);                      // TODO: implement security
        mtuSize = buf.readShort();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverGuid", serverGuid)
                .append("hasSecurity", hasSecurity)
                .append("mtuSize", mtuSize)
                .toString();
    }
}
