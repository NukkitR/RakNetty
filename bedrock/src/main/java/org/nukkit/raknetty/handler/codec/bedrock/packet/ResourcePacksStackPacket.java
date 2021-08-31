package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.Experiment;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackInstanceId;
import org.nukkit.raknetty.handler.codec.bedrock.data.SemVersion;
import org.nukkit.raknetty.util.VarIntUtil;

import java.util.Collection;

public class ResourcePacksStackPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    /**
     * Force clients to use texture packs in the current world
     */
    public boolean isTexturePacksRequired;
    public Collection<PackInstanceId> behaviorPacksInfo;
    public Collection<PackInstanceId> resourcePacksInfo;
    public SemVersion baseGameVersion;
    public Collection<Experiment> experiments;
    boolean wereAnyExperimentsEverToggled;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACKS_STACK;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        // lambda_cb039857828abad34c85c26a065e4638_::operator()
        buf.writeBoolean(isTexturePacksRequired);
        BedrockPacketUtil.writeList(buf, behaviorPacksInfo, (length) -> {
            VarIntUtil.writeUnsignedVarInt(buf, length);
        });
        BedrockPacketUtil.writeList(buf, resourcePacksInfo, (length) -> {
            VarIntUtil.writeUnsignedVarInt(buf, length);
        });
        BedrockPacketUtil.writeString(buf, baseGameVersion.toString());
        BedrockPacketUtil.writeList(buf, experiments, buf::writeIntLE);
        buf.writeBoolean(wereAnyExperimentsEverToggled);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        isTexturePacksRequired = buf.readBoolean();
        behaviorPacksInfo = BedrockPacketUtil.readList(buf, PackInstanceId.class, () -> VarIntUtil.readUnsignedVarInt(buf));
        resourcePacksInfo = BedrockPacketUtil.readList(buf, PackInstanceId.class, () -> VarIntUtil.readUnsignedVarInt(buf));
        baseGameVersion = new SemVersion(BedrockPacketUtil.readString(buf));
        experiments = BedrockPacketUtil.readList(buf, Experiment.class, () -> VarIntUtil.readUnsignedVarInt(buf));
        wereAnyExperimentsEverToggled = buf.readBoolean();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isTexturePacksRequired", isTexturePacksRequired)
                .append("behaviorPacksInfo", behaviorPacksInfo)
                .append("resourcePacksInfo", resourcePacksInfo)
                .append("baseGameVersion", baseGameVersion)
                .append("experiments", experiments)
                .append("wereAnyExperimentsEverToggled", wereAnyExperimentsEverToggled)
                .toString();
    }
}
