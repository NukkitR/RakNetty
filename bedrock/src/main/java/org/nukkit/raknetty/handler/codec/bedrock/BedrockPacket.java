package org.nukkit.raknetty.handler.codec.bedrock;

import org.nukkit.raknetty.handler.codec.Message;

public interface BedrockPacket extends Message {

    PacketIdentifier getId();

    int getClientId();

    int getSenderId();
}
