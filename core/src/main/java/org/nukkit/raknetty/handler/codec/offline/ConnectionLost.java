package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.ByteUtil;

public class ConnectionLost implements OfflineMessage {

    @Override
    public void encode(ByteBuf buf) {
        ByteUtil.writeByte(buf, MessageIdentifier.ID_CONNECTION_LOST);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
    }
}
