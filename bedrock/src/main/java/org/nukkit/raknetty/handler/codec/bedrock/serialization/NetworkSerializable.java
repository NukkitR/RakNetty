package org.nukkit.raknetty.handler.codec.bedrock.serialization;

import org.nukkit.raknetty.buffer.BedrockByteBuf;

public interface NetworkSerializable {

    void encode(BedrockByteBuf buf) throws Exception;

    void decode(BedrockByteBuf buf) throws Exception;
}
