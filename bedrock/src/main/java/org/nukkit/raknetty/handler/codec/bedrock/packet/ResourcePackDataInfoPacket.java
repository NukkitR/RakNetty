package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.api.packs.PackIdVersion;
import org.nukkit.api.packs.PackType;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class ResourcePackDataInfoPacket implements ServerBedrockPacket {

    public PackIdVersion packIdVersion;
    public int chunkSize;
    public int chunkCount;
    public long fileSize;
    public byte[] checksum; //CryptoUtils::getFileChecksum
    public boolean isPremium;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACK_DATA_INFO;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeString(packIdVersion.toString());
        buf.writeIntLE(chunkSize);
        buf.writeIntLE(chunkCount);
        buf.writeLongLE(fileSize);
        buf.writeBytes(checksum);
        buf.writeBoolean(isPremium);
        buf.writeEnum(packIdVersion.packType());
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        String str = buf.readString();
        chunkSize = buf.readIntLE();
        chunkCount = buf.readIntLE();
        fileSize = buf.readLongLE();
        int len = buf.readUnsignedVarInt();
        checksum = new byte[len];
        buf.readBytes(checksum);
        isPremium = buf.readBoolean();
        PackType packType = buf.readEnum(PackType.class);
        packIdVersion = PackIdVersion.fromString(str, packType);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("packIdVersion", packIdVersion)
                .append("chunkSize", chunkSize)
                .append("chunkCount", chunkCount)
                .append("fileSize", fileSize)
                .append("checksum", ByteBufUtil.hexDump(checksum))
                .append("isPremium", isPremium)
                .append("packType", packIdVersion.packType())
                .toString();
    }
}
