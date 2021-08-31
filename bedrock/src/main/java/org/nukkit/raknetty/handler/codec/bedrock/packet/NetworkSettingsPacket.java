package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class NetworkSettingsPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public int compressionThreshold;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.NETWORK_SETTINGS;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeShortLE(compressionThreshold);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        compressionThreshold = buf.readUnsignedShortLE();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("compressionThreshold", compressionThreshold)
                .toString();
    }
}
