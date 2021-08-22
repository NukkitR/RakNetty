package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.DisconnectReason;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class DisconnectPacket extends AbstractBedrockPacket {

    public boolean noDisconnectionScreen = false;
    public DisconnectReason reason = DisconnectReason.DISCONNECTED;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.DISCONNECT;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeBoolean(noDisconnectionScreen);
        if (!noDisconnectionScreen) {
            BedrockPacketUtil.writeString(buf, reason.toString());
        }
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        noDisconnectionScreen = buf.readBoolean();
        if (!noDisconnectionScreen) {
            String text = BedrockPacketUtil.readString(buf);
            reason = DisconnectReason.fromString(text);
            Validate.notNull(reason, "Unknown reason: " + text);
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
