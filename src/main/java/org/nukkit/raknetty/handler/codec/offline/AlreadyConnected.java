package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

public class AlreadyConnected implements OfflineMessage {

    public long senderGuid;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_ALREADY_CONNECTED;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(senderGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        senderGuid = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("senderGuid", senderGuid)
                .toString();
    }
}
