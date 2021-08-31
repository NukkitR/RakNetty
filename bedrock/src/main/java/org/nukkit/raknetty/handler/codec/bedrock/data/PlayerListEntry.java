package org.nukkit.raknetty.handler.codec.bedrock.data;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.util.VarIntUtil;

import java.util.UUID;

public class PlayerListEntry implements ByteBufSerializable {

    public UUID uuid;
    public long uniqueId;
    public String name;
    public String xuid;
    public String platformOnlineId; //Player::getPlatformOnlineId
    public int platform;            //Player::getPlatform
    public SkinData skinData;
    public boolean isTeacher;       //ServerPlayer::isTeacher
    public boolean isHostingPlayer; //ServerPlayer::isHostingPlayer

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeLongLE(uuid.getMostSignificantBits());
        buf.writeLongLE(uuid.getLeastSignificantBits());
        VarIntUtil.writeVarLong(buf, uniqueId);
        BedrockPacketUtil.writeString(buf, name);
        BedrockPacketUtil.writeString(buf, xuid);
        BedrockPacketUtil.writeString(buf, platformOnlineId);
        buf.writeIntLE(platform);
        skinData.encode(buf);
        buf.writeBoolean(isTeacher);
        buf.writeBoolean(isHostingPlayer);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        uuid = new UUID(buf.readLongLE(), buf.readLongLE());
        uniqueId = VarIntUtil.readVarLong(buf);
        name = BedrockPacketUtil.readString(buf);
        xuid = BedrockPacketUtil.readString(buf);
        platformOnlineId = BedrockPacketUtil.readString(buf);
        platform = buf.readIntLE();
        skinData = new SkinData();
        skinData.decode(buf);
        isTeacher = buf.readBoolean();
        isHostingPlayer = buf.readBoolean();

        System.out.println(ByteBufUtil.prettyHexDump(buf));
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("uuid", uuid)
                .append("uniqueId", uniqueId)
                .append("name", name)
                .append("xuid", xuid)
                .append("platformOnlineId", platformOnlineId)
                .append("platform", platform)
                .append("skinData", skinData)
                .append("isTeacher", isTeacher)
                .append("isHostingPlayer", isHostingPlayer)
                .toString();
    }
}
