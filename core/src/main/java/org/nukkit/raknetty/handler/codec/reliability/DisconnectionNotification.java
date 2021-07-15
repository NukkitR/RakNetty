package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class DisconnectionNotification implements ReliabilityMessage {

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_DISCONNECTION_NOTIFICATION);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
