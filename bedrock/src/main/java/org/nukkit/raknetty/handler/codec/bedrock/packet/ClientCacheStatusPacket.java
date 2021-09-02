package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class ClientCacheStatusPacket implements ClientBedrockPacket {

    public boolean isEnabled;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.CLIENT_CACHE_STATUS;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeBoolean(isEnabled);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        isEnabled = buf.readBoolean();
    }
}
