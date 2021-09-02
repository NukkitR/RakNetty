package org.nukkit.raknetty.handler.codec.bedrock.serialization;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;

public class NetworkItemStack implements NetworkSerializable {

    public int id;
    public int stackSize;
    public int auxValue;
    public boolean hasServerNetId;
    /**
     * ItemStackNetIdVariant::tryGetServerNetId
     * Server NetId, -1, 0, 1 or 2
     */
    public int serverNetId;
    public int runtimeId;
    public String name;

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        if (id <= 0) {
            buf.writeVarInt(0);
            return;
        }

        buf.writeVarInt(id);
        buf.writeShortLE(stackSize);
        buf.writeUnsignedVarInt(auxValue);

        buf.writeBoolean(hasServerNetId);
        if (serverNetId == 1 || serverNetId == 2) {
            buf.writeVarInt(serverNetId);
        } else {
            buf.writeUnsignedVarInt(0);
        }
        buf.writeVarInt(runtimeId);
        buf.writeString(name);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        id = buf.readVarInt();
        if (id <= 0) return;

        stackSize = buf.readUnsignedShortLE();
        auxValue = buf.readUnsignedVarInt();

        hasServerNetId = buf.readBoolean();
        if (hasServerNetId) {
            serverNetId = buf.readVarInt();
        } else {
            serverNetId = 0;
        }
        runtimeId = buf.readVarInt();
        name = buf.readString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("stackSize", stackSize)
                .append("auxValue", auxValue)
                .append("hasServerNetId", hasServerNetId)
                .append("serverNetId", serverNetId)
                .append("runtimeId", runtimeId)
                .append("name", name)
                .toString();
    }
}
