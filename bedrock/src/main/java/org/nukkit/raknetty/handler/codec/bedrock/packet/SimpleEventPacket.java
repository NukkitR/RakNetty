package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class SimpleEventPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public int eventType;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.SIMPLE_EVENT;
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        eventType = buf.readUnsignedShortLE();
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeShortLE(eventType);
    }
}
