package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class IpRecentlyConnected implements OfflineMessage {

    public long senderGuid;

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_IP_RECENTLY_CONNECTED);
        buf.writeBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID);
        buf.writeLong(senderGuid);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        buf.skipBytes(OfflineMessage.OFFLINE_MESSAGE_DATA_ID.length);
        senderGuid = buf.readLong();
    }

}
