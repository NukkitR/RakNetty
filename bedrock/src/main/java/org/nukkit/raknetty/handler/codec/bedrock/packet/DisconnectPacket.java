package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.DisconnectReason;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.ProtocolUtil;

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
            ProtocolUtil.writeString(buf, reason.toString());
        }
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        noDisconnectionScreen = buf.readBoolean();
        if (!noDisconnectionScreen) {
            String text = ProtocolUtil.readString(buf);
            reason = DisconnectReason.fromString(text);
            Validate.notNull(reason, "Unknown reason: " + text);
        }
    }
}
