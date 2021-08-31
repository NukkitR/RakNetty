package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class ClientCacheStatusPacket extends AbstractBedrockPacket implements ClientBedrockPacket {

    public boolean isEnabled;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.CLIENT_CACHE_STATUS;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeBoolean(isEnabled);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        isEnabled = buf.readBoolean();
    }
}
