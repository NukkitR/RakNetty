package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public interface BedrockPacket {

    PacketIdentifier getId();

    void encode(BedrockByteBuf buf) throws Exception;

    void decode(BedrockByteBuf buf) throws Exception;
}
