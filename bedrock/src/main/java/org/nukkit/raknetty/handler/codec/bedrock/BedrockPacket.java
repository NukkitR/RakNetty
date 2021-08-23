package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;

public interface BedrockPacket {

    PacketIdentifier getId();

    int getClientId();

    int getSenderId();

    void encode(ByteBuf buf) throws Exception;

    void decode(ByteBuf buf) throws Exception;
}
