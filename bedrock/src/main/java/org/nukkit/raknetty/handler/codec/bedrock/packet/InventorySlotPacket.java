package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.NetworkItemStack;

public class InventorySlotPacket implements ServerBedrockPacket, ClientBedrockPacket {

    public int containerId; //+48
    public int slot; //+52
    public NetworkItemStack item; //+56

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.INVENTORY_SLOT;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeUnsignedVarInt(containerId);
        buf.writeUnsignedVarInt(slot);
        item.encode(buf);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        containerId = buf.readUnsignedVarInt();
        slot = buf.readUnsignedVarInt();
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
