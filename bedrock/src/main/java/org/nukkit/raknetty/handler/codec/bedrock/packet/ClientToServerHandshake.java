package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class ClientToServerHandshake extends AbstractBedrockPacket implements ClientBedrockPacket {

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.CLIENT_TO_SERVER_HANDSHAKE;
    }

    @Override
    public void encode(ByteBuf buf) {
    }

    @Override
    public void decode(ByteBuf buf) {
    }
}
