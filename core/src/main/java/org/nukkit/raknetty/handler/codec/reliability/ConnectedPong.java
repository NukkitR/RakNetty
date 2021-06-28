package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class ConnectedPong implements ReliabilityMessage {

    public long pingTime;
    public long pongTime;

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_CONNECTED_PONG);
        buf.writeLong(pingTime);
        buf.writeLong(pongTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
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
