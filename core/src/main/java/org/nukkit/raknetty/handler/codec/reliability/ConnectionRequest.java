package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.util.PacketUtil;

public class ConnectionRequest implements ReliabilityMessage {

    public long clientGuid;
    public long requestTime;

    @Override
    public void encode(ByteBuf buf) {
        PacketUtil.writeByte(buf, MessageIdentifier.ID_CONNECTION_REQUEST);
        buf.writeLong(clientGuid);
        buf.writeLong(requestTime);
        //TODO: security
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        clientGuid = buf.readLong();
        requestTime = buf.readLong();
    }
}
