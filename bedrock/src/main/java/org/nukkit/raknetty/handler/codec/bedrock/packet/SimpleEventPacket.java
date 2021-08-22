package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;

abstract class SimpleEventPacket extends AbstractBedrockPacket {

    public abstract int get();

    public abstract void set(int data);

    @Override
    public void decode(ByteBuf buf) throws Exception {
        set(buf.readUnsignedShortLE());
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeShortLE(get());
    }
}
