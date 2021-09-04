package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

public class ConnectedPing implements ReliabilityMessage {

    public long pingTime;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_CONNECTED_PING;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(pingTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        pingTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pingTime", pingTime)
                .toString();
    }
}
