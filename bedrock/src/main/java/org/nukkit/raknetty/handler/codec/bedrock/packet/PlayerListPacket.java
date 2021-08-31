package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.PlayerListEntry;
import org.nukkit.raknetty.util.VarIntUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class PlayerListPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public enum Type {
        ADD,
        REMOVE
    }

    public Type type;
    public Collection<PlayerListEntry> entries;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.PLAYER_LIST;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        Validate.notNull(type);
        buf.writeByte(type.ordinal());
        switch (type) {
            case ADD: {
                BedrockPacketUtil.writeList(buf, entries, len -> VarIntUtil.writeUnsignedVarInt(buf, len));
                break;
            }
            case REMOVE: {
                int len = entries.size();
                VarIntUtil.writeUnsignedVarInt(buf, len);
                for (PlayerListEntry entry : entries) {
                    buf.writeLongLE(entry.uuid.getMostSignificantBits());
                    buf.writeLongLE(entry.uuid.getLeastSignificantBits());
                }
            }
        }
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        type = Type.values()[buf.readByte()];
        if (type == null) return;

        int len = (int) VarIntUtil.readUnsignedVarInt(buf);

        switch (type) {
            case ADD: {
                entries = BedrockPacketUtil.readList(buf, PlayerListEntry.class, () -> len);
                break;
            }
            case REMOVE: {

                entries = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    PlayerListEntry entry = new PlayerListEntry();
                    entry.uuid = new UUID(buf.readLongLE(), buf.readLongLE());
                    entries.add(entry);
                }
                break;
            }
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("type", type)
                .append("entries", entries)
                .toString();
    }
}
