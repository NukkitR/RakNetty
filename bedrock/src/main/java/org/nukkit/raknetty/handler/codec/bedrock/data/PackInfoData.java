package org.nukkit.raknetty.handler.codec.bedrock.data;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

import java.util.UUID;

public interface PackInfoData extends ByteBufSerializable {

    class Behavior implements PackInfoData {
        public UUID packId;             //PackInstance::getPackId
        public SemVersion version;          //PackInstance::getVersion
        public long packSize;           //PackManifest::getPackSize
        public String contentKey;       //see https://gist.github.com/MagicDroidX/74cedd907e254f85fa03ae64f62e4360
        public String subpackFolderName;//PackInstance::getSubpackFolderName
        public UUID contentIdentity;    //PackManifest::getContentIdentity
        public boolean hasClientData;   //see https://minecraft.fandom.com/wiki/Bedrock_Edition_beta_scripting_documentation


        @Override
        public void encode(ByteBuf buf) {
            BedrockPacketUtil.writeString(buf, packId.toString());
            BedrockPacketUtil.writeString(buf, version.toString());
            buf.writeLongLE(packSize);
            BedrockPacketUtil.writeString(buf, contentKey);
            BedrockPacketUtil.writeString(buf, subpackFolderName);
            BedrockPacketUtil.writeString(buf, contentIdentity.toString());
            buf.writeBoolean(hasClientData);
        }

        @Override
        public void decode(ByteBuf buf) {
            packId = UUID.fromString(BedrockPacketUtil.readString(buf));
            version = new SemVersion(BedrockPacketUtil.readString(buf));
            packSize = buf.readLongLE();
            contentKey = BedrockPacketUtil.readString(buf);
            subpackFolderName = BedrockPacketUtil.readString(buf);
            contentIdentity = UUID.fromString(BedrockPacketUtil.readString(buf));
            hasClientData = buf.readBoolean();
        }

        public PackIdVersion toIdVersion() {
            return new PackIdVersion(packId, version);
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

    class Resource extends Behavior implements PackInfoData {
        public boolean hasRayTracing;   //PackManifest::hasPackCapability(RayTracingOptions::RAY_TRACING_TAG)

        @Override
        public void encode(ByteBuf buf) {
            super.encode(buf);
            buf.writeBoolean(hasRayTracing);
        }

        @Override
        public void decode(ByteBuf buf) {
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
