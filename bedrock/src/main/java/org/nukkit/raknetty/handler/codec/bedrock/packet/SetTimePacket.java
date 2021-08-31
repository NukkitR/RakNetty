package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.util.VarIntUtil;

public class SetTimePacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public int time;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.SET_TIME;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        VarIntUtil.writeVarInt(buf, time);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        time = VarIntUtil.readVarInt(buf);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("time", time)
                .toString();
    }
}
