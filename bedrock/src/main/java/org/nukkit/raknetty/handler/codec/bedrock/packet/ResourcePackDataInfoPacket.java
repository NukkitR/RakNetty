package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackIdVersion;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackType;
import org.nukkit.raknetty.util.VarIntUtil;

public class ResourcePackDataInfoPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public PackIdVersion packIdVersion;
    public int chunkSize;
    public int chunkCount;
    public long fileSize;
    public byte[] checksum; //CryptoUtils::getFileChecksum
    public boolean isPremium;
    public PackType packType;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACK_DATA_INFO;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        BedrockPacketUtil.writeString(buf, packIdVersion.toString());
        buf.writeIntLE(chunkSize);
        buf.writeIntLE(chunkCount);
        buf.writeLongLE(fileSize);
        buf.writeBytes(checksum);
        buf.writeBoolean(isPremium);
        buf.writeByte(packType.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        packIdVersion = new PackIdVersion(BedrockPacketUtil.readString(buf));
        chunkSize = buf.readIntLE();
        chunkCount = buf.readIntLE();
        fileSize = buf.readLongLE();
        int len = (int) VarIntUtil.readUnsignedVarInt(buf);
        checksum = new byte[len];
        buf.readBytes(checksum);
        isPremium = buf.readBoolean();
        packType = PackType.valueOf(buf.readUnsignedByte());
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
                .append("packType", packType)
                .toString();
    }
}
