package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class NetworkSettingsPacket extends SimpleEventPacket {

    public int compressionThreshold;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.NETWORK_SETTINGS;
    }

    @Override
    public int get() {
        return compressionThreshold;
    }

    @Override
    public void set(int data) {
        compressionThreshold = data;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("compressionThreshold", compressionThreshold)
                .toString();
    }
}
