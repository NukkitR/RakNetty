package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

public class NoFreeIncomingConnection implements OfflineMessage {

    public long senderGuid;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_NO_FREE_INCOMING_CONNECTIONS;
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
}
