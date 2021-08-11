package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.ByteUtil;

public class UnconnectedPing implements OfflineMessage {

    public long sendPingTime;
    public long senderGuid;

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_UNCONNECTED_PING);
        buf.writeLong(sendPingTime);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(senderGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
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
