package org.nukkit.raknetty.handler.codec.bedrock;

import org.nukkit.raknetty.handler.codec.ByteBufSerializable;

public interface BedrockPacket extends ByteBufSerializable {

    PacketIdentifier getId();

    int getClientId();

    int getSenderId();
}
