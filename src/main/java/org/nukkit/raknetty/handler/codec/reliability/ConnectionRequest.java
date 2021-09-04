package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

public class ConnectionRequest implements ReliabilityMessage {

    public long clientGuid;
    public long requestTime;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_CONNECTION_REQUEST;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(clientGuid);
        buf.writeLong(requestTime);
        buf.writeBoolean(false); //TODO: security
    }

    @Override
    public void decode(ByteBuf buf) {
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
