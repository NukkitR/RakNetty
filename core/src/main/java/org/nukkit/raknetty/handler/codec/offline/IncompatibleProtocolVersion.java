package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class IncompatibleProtocolVersion implements OfflineMessage {

    public int protocol;
    public long senderGuid;

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_INCOMPATIBLE_PROTOCOL_VERSION);
        buf.writeByte(protocol);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(senderGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        protocol = buf.readByte();
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        senderGuid = buf.readLong();
    }
}
