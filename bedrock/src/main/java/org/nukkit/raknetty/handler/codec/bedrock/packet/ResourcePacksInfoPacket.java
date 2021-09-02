package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.NetworkSerializable;

import java.util.Collection;
import java.util.UUID;

public class ResourcePacksInfoPacket implements ServerBedrockPacket {

    public boolean isTexturePacksRequired;
    protected final boolean unused = false;
    public Collection<BehaviorPackInfo> behaviorPacksInfo;
    public Collection<ResourcePackInfo> resourcePacksInfo;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACKS_INFO;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeBoolean(isTexturePacksRequired);
        buf.writeBoolean(hasClientData());
        buf.writeBoolean(unused);
        buf.writeList(behaviorPacksInfo, buf::writeShortLE);
        buf.writeList(resourcePacksInfo, buf::writeShortLE);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        isTexturePacksRequired = buf.readBoolean();
        boolean hasClientData = buf.readBoolean();
        buf.readBoolean();
        behaviorPacksInfo = buf.readList(BehaviorPackInfo::new, buf::readUnsignedShortLE);
        resourcePacksInfo = buf.readList(ResourcePackInfo::new, buf::readUnsignedShortLE);

        Validate.isTrue(hasClientData == hasClientData());
    }

    public final boolean hasClientData() {
        if (behaviorPacksInfo == null) return false;
        return behaviorPacksInfo.stream().anyMatch(packInfo -> packInfo.hasClientData);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isTexturePacksRequired", isTexturePacksRequired)
                .append("hasClientData", hasClientData())
                .append("behaviorPacksInfo", behaviorPacksInfo)
                .append("resourcePacksInfo", resourcePacksInfo)
                .toString();
    }

    public static class BehaviorPackInfo implements NetworkSerializable {
        public UUID packId;             //PackInstance::getPackId
        public String version;          //PackInstance::getVersion
        public long packSize;           //PackManifest::getPackSize
        public String contentKey;       //see https://gist.github.com/MagicDroidX/74cedd907e254f85fa03ae64f62e4360
        public String subpackFolderName;//PackInstance::getSubpackFolderName
        public UUID contentIdentity;    //PackManifest::getContentIdentity
        public boolean hasClientData;   //see https://minecraft.fandom.com/wiki/Bedrock_Edition_beta_scripting_documentation

        @Override
        public void encode(BedrockByteBuf buf) {
            buf.writeString(packId.toString());
            buf.writeString(version);
            buf.writeLongLE(packSize);
            buf.writeString(contentKey);
            buf.writeString(subpackFolderName);
            buf.writeString(contentIdentity.toString());
            buf.writeBoolean(hasClientData);
        }

        @Override
        public void decode(BedrockByteBuf buf) {
            packId = UUID.fromString(buf.readString());
            version = buf.readString();
            packSize = buf.readLongLE();
            contentKey = buf.readString();
            subpackFolderName = buf.readString();
            contentIdentity = UUID.fromString(buf.readString());
            hasClientData = buf.readBoolean();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("packId", packId)
                    .append("version", version)
                    .append("packSize", packSize)
                    .append("contentKey", contentKey)
                    .append("subpackFolderName", subpackFolderName)
                    .append("contentIdentity", contentIdentity)
                    .append("hasClientData", hasClientData)
                    .toString();
        }
    }

    public static class ResourcePackInfo extends BehaviorPackInfo implements NetworkSerializable {
        public boolean hasRayTracing;   //PackManifest::hasPackCapability(RayTracingOptions::RAY_TRACING_TAG)

        @Override
        public void encode(BedrockByteBuf buf) {
            super.encode(buf);
            buf.writeBoolean(hasRayTracing);
        }

        @Override
        public void decode(BedrockByteBuf buf) {
            super.decode(buf);
            hasRayTracing = buf.readBoolean();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .appendSuper(super.toString())
                    .append("hasRayTracing", hasRayTracing)
                    .toString();
        }
    }

}
