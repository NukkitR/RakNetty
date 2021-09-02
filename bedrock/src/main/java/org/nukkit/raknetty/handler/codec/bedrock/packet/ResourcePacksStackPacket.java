package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.api.SemVersion;
import org.nukkit.api.packs.PackInstanceId;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.Experiments;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.NetworkSerializer;

import java.util.Collection;
import java.util.UUID;

public class ResourcePacksStackPacket implements ServerBedrockPacket {

    private final static PackInstanceIdSerializer SERIALIZER = new PackInstanceIdSerializer();

    /**
     * Force clients to use texture packs in the current world
     */
    public boolean isTexturePacksRequired;
    public Collection<PackInstanceId> behaviorPackStack;
    public Collection<PackInstanceId> resourcePackStack;
    public SemVersion baseGameVersion;
    public Experiments experiments;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACKS_STACK;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeBoolean(isTexturePacksRequired);
        buf.writeList(behaviorPackStack, SERIALIZER, buf::writeUnsignedVarInt);
        buf.writeList(resourcePackStack, SERIALIZER, buf::writeUnsignedVarInt);
        buf.writeString(baseGameVersion.asString());
        experiments.encode(buf);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        isTexturePacksRequired = buf.readBoolean();
        behaviorPackStack = buf.readList(SERIALIZER, buf::readUnsignedVarInt);
        resourcePackStack = buf.readList(SERIALIZER, buf::readUnsignedVarInt);
        baseGameVersion = SemVersion.fromString(buf.readString());
        experiments = new Experiments();
        experiments.decode(buf);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isTexturePacksRequired", isTexturePacksRequired)
                .append("behaviorPacksInfo", behaviorPackStack)
                .append("resourcePacksInfo", resourcePackStack)
                .append("baseGameVersion", baseGameVersion)
                .append("experiments", experiments)
                .toString();
    }

    private static class PackInstanceIdSerializer implements NetworkSerializer<PackInstanceId> {
        @Override
        public void encode(BedrockByteBuf buf, PackInstanceId packInstanceId) throws Exception {
            buf.writeString(packInstanceId.getPackId().toString());
            buf.writeString(packInstanceId.getVersion());
            buf.writeString(packInstanceId.getSubpackFolderName());
        }

        @Override
        public PackInstanceId decode(BedrockByteBuf buf) throws Exception {
            String uuid = buf.readString();
            String version = buf.readString();
            String subpackFolderName = buf.readString();
            return new PackInstanceId(UUID.fromString(uuid), version, subpackFolderName);
        }
    }
}
