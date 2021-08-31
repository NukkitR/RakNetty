package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackInfoData;

import java.util.Collection;

public class ResourcePacksInfoPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public boolean isTexturePacksRequired;
    protected final boolean unused = false;
    public Collection<PackInfoData.Behavior> behaviorPacksInfo;
    public Collection<PackInfoData.Resource> resourcePacksInfo;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACKS_INFO;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeBoolean(isTexturePacksRequired);
        buf.writeBoolean(hasClientData());
        buf.writeBoolean(unused);
        BedrockPacketUtil.writeList(buf, behaviorPacksInfo, buf::writeShortLE);
        BedrockPacketUtil.writeList(buf, resourcePacksInfo, buf::writeShortLE);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        isTexturePacksRequired = buf.readBoolean();
        boolean hasClientData = buf.readBoolean();
        buf.readBoolean();
        behaviorPacksInfo = BedrockPacketUtil.readList(buf, PackInfoData.Behavior.class, buf::readUnsignedShortLE);
        resourcePacksInfo = BedrockPacketUtil.readList(buf, PackInfoData.Resource.class, buf::readUnsignedShortLE);

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
}
