package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.ByteUtil;

public class UnconnectedPong implements OfflineMessage {

    public long sendPingTime;
    public long senderGuid;
    public ByteBuf response;

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_UNCONNECTED_PONG);
        buf.writeLong(sendPingTime);
        buf.writeLong(senderGuid);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeBytes(response, response.readerIndex(), response.readableBytes());
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        sendPingTime = buf.readLong();
        senderGuid = buf.readLong();
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        response = buf.slice();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sendPingTime", sendPingTime)
                .append("senderGuid", senderGuid)
                .append("response", ByteBufUtil.prettyHexDump(response))
                .toString();
    }
}
