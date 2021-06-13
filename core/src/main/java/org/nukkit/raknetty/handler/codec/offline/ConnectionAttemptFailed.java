package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class ConnectionAttemptFailed implements OfflineMessage {

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_CONNECTION_ATTEMPT_FAILED);
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
    }
}
