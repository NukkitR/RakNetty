package org.nukkit.raknetty.handler.codec.bedrock.data;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

import java.util.UUID;

public final class PackInstanceId extends PackIdVersion implements ByteBufSerializable {

    private String subpackFolderName;

    public PackInstanceId() {
        super(null, null);
    }

    public PackInstanceId(PackIdVersion idVersion, String subpackFolderName) {
        super(idVersion.packId(), idVersion.version());
        this.subpackFolderName = subpackFolderName;
    }

    public String subpackFolderName() {
        return this.subpackFolderName;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        BedrockPacketUtil.writeString(buf, packId.toString());
        BedrockPacketUtil.writeString(buf, version.toString());
        BedrockPacketUtil.writeString(buf, subpackFolderName);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        packId = UUID.fromString(BedrockPacketUtil.readString(buf));
        version = new SemVersion(BedrockPacketUtil.readString(buf));
        subpackFolderName = BedrockPacketUtil.readString(buf);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("packId", packId)
                .append("version", version)
                .append("subpackFolderName", subpackFolderName)
                .toString();
    }
}
