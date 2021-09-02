package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.PlayerListEntry;

import java.util.ArrayList;
import java.util.Collection;

public class PlayerListPacket implements ServerBedrockPacket {

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
    public void encode(BedrockByteBuf buf) throws Exception {
        Validate.notNull(type);
        buf.writeEnum(type);
        switch (type) {
            case ADD: {
                buf.writeList(entries, buf::writeUnsignedVarInt);
                break;
            }
            case REMOVE: {
                int len = entries.size();
                buf.writeUnsignedVarInt(len);
                for (PlayerListEntry entry : entries) {
                    buf.writeUUID(entry.uuid);
                }
            }
        }
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        type = buf.readEnum(Type.class);
        if (type == null) return;

        int len = buf.readUnsignedVarInt();

        switch (type) {
            case ADD: {
                entries = buf.readList(PlayerListEntry::new, () -> len);
                break;
            }
            case REMOVE: {

                entries = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    PlayerListEntry entry = new PlayerListEntry();
                    entry.uuid = buf.readUUID();
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
