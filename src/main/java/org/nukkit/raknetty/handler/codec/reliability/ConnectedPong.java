package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

public class ConnectedPong implements ReliabilityMessage {

    public long pingTime;
    public long pongTime;

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_CONNECTED_PONG;
    }

    @Override
    public void encode(ByteBuf buf) {
        buf.writeLong(pingTime);
        buf.writeLong(pongTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        pingTime = buf.readLong();
        pongTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pingTime", pingTime)
                .append("pongTime", pongTime)
                .toString();
    }
}
