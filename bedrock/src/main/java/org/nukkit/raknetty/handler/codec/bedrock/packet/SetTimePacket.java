package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class SetTimePacket implements ServerBedrockPacket {

    public int time;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.SET_TIME;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeVarInt(time);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        time = buf.readVarInt();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("time", time)
                .toString();
    }
}
