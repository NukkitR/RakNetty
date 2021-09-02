package org.nukkit.raknetty.handler.codec.bedrock.serialization;

import org.nukkit.raknetty.buffer.BedrockByteBuf;

public interface NetworkSerializer<T> {

    void encode(BedrockByteBuf buf, T t) throws Exception;

    T decode(BedrockByteBuf buf) throws Exception;

}
