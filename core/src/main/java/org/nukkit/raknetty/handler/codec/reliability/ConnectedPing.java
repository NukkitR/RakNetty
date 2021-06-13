package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class ConnectedPing implements ReliabilityMessage {

    public long pingTime;

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_CONNECTED_PING);
        buf.writeLong(pingTime);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        pingTime = buf.readLong();
    }
}
