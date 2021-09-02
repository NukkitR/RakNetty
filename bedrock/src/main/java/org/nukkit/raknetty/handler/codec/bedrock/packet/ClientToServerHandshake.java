package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class ClientToServerHandshake implements ClientBedrockPacket {

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.CLIENT_TO_SERVER_HANDSHAKE;
    }

    @Override
    public void encode(BedrockByteBuf buf) {
    }

    @Override
    public void decode(BedrockByteBuf buf) {
    }
}
