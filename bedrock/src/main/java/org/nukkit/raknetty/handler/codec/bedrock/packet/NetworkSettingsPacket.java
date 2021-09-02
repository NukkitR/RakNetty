package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class NetworkSettingsPacket implements ServerBedrockPacket {

    public int compressionThreshold;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.NETWORK_SETTINGS;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeShortLE(compressionThreshold);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        compressionThreshold = buf.readUnsignedShortLE();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("compressionThreshold", compressionThreshold)
                .toString();
    }
}
