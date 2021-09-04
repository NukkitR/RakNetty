package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;

public interface ByteBufSerializable {

    void encode(ByteBuf buf) throws Exception;

    void decode(ByteBuf buf) throws Exception;
}
