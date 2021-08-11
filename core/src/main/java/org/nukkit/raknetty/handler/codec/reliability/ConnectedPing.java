package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.ByteUtil;

public class ConnectedPing implements ReliabilityMessage {

    public long pingTime;

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_CONNECTED_PING);
        buf.writeLong(pingTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        pingTime = buf.readLong();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pingTime", pingTime)
                .toString();
    }
}
