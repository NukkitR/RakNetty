package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.ByteUtil;

public class ConnectionRequest implements ReliabilityMessage {

    public long clientGuid;
    public long requestTime;

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_CONNECTION_REQUEST);
        buf.writeLong(clientGuid);
        buf.writeLong(requestTime);
        buf.writeBoolean(false); //TODO: security
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        clientGuid = buf.readLong();
        requestTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("clientGuid", clientGuid)
                .append("requestTime", requestTime)
                .toString();
    }
}
