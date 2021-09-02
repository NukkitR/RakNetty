package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class SimpleEventPacket implements ServerBedrockPacket {

    public int eventType;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.SIMPLE_EVENT;
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        eventType = buf.readUnsignedShortLE();
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeShortLE(eventType);
    }
}
