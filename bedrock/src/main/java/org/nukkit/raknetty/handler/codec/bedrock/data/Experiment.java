package org.nukkit.raknetty.handler.codec.bedrock.data;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

public class Experiment implements ByteBufSerializable {

    public String name;
    public boolean isEnabled;

    @Override
    public void encode(ByteBuf buf) throws Exception {
        BedrockPacketUtil.writeString(buf, name);
        buf.writeBoolean(isEnabled);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        name = BedrockPacketUtil.readString(buf);
        isEnabled = buf.readBoolean();
    }
}
