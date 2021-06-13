package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;

public interface Message {

    int RAKNET_PROTOCOL_VERSION = 10;

    int UDP_HEADER_SIZE = 28;

    void encode(ByteBuf buf);

    void decode(ByteBuf buf);
}
