package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.DisconnectReason;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class DisconnectPacket implements ServerBedrockPacket {

    public boolean noDisconnectionScreen = false;
    public DisconnectReason reason = DisconnectReason.DISCONNECTED;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.DISCONNECT;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeBoolean(noDisconnectionScreen);
        if (!noDisconnectionScreen) {
            buf.writeString(reason.toString());
        }
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        noDisconnectionScreen = buf.readBoolean();
        if (!noDisconnectionScreen) {
            String id = buf.readString();
            reason = DisconnectReason.findById(id);
            Validate.notNull(reason, "Unknown reason: " + id);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("noDisconnectionScreen", noDisconnectionScreen)
                .append("reason", reason)
                .toString();
    }
}
