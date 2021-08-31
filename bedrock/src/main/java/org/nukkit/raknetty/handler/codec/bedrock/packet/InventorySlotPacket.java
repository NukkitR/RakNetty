package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.NetworkItemStack;
import org.nukkit.raknetty.util.VarIntUtil;

public class InventorySlotPacket extends AbstractBedrockPacket implements ServerBedrockPacket, ClientBedrockPacket {

    public int containerId; //+48
    public int slot; //+52
    public NetworkItemStack item; //+56

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.INVENTORY_SLOT;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        VarIntUtil.writeUnsignedVarInt(buf, containerId);
        VarIntUtil.writeUnsignedVarInt(buf, slot);
        item.encode(buf);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        containerId = (int) VarIntUtil.readUnsignedVarInt(buf);
        slot = (int) VarIntUtil.readUnsignedVarInt(buf);
        item = new NetworkItemStack();
        item.decode(buf);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("containerId", containerId)
                .append("slot", slot)
                .append("item", item)
                .toString();
    }
}
