package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

public class UnconnectedPing implements OfflineMessage {

    public long sendPingTime;
    public long senderGuid;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_UNCONNECTED_PING;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(sendPingTime);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(senderGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        sendPingTime = buf.readLong();
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        senderGuid = buf.readLong();
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sendPingTime", sendPingTime)
                .append("senderGuid", senderGuid)
                .toString();
    }
}
