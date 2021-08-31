package org.nukkit.raknetty.handler.codec.bedrock.data;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.util.VarIntUtil;

public class NetworkItemStack implements ByteBufSerializable {

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
    public void encode(ByteBuf buf) throws Exception {
        if (id <= 0) {
            VarIntUtil.writeVarInt(buf, 0);
            return;
        }

        VarIntUtil.writeVarInt(buf, id);
        buf.writeShortLE(stackSize);
        VarIntUtil.writeUnsignedVarInt(buf, auxValue);

        buf.writeBoolean(hasServerNetId);
        if (serverNetId == 1 || serverNetId == 2) {
            VarIntUtil.writeVarInt(buf, serverNetId);
        } else {
            VarIntUtil.writeUnsignedVarInt(buf, 0);
        }
        VarIntUtil.writeVarInt(buf, runtimeId);
        BedrockPacketUtil.writeString(buf, name);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        id = VarIntUtil.readVarInt(buf);
        if (id <= 0) return;

        stackSize = buf.readUnsignedShortLE();
        auxValue = (int) VarIntUtil.readUnsignedVarInt(buf);

        hasServerNetId = buf.readBoolean();
        serverNetId = VarIntUtil.readVarInt(buf);
        runtimeId = VarIntUtil.readVarInt(buf);
        name = BedrockPacketUtil.readString(buf);
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
