package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;

public interface Message {

    int RAKNET_PROTOCOL_VERSION = 10;

    int UDP_HEADER_SIZE = 28;

    int MESSAGE_HEADER_MAX_SIZE = 1 + 2 + 3 + 3 + 3 + 1 + 4 + 2 + 4;

    MessageIdentifier getId();

    void encode(ByteBuf buf) throws Exception;

    void decode(ByteBuf buf) throws Exception;
}
