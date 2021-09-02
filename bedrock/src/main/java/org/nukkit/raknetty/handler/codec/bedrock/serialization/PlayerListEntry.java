package org.nukkit.raknetty.handler.codec.bedrock.serialization;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;

import java.util.UUID;

public class PlayerListEntry implements NetworkSerializable {

    public UUID uuid;
    public long uniqueId;
    public String name;
    public String xuid;
    public String platformOnlineId; //Player::getPlatformOnlineId
    public int platform;            //Player::getPlatform
    public SerializedSkin skinData;
    public boolean isTeacher;       //ServerPlayer::isTeacher
    public boolean isHostingPlayer; //ServerPlayer::isHostingPlayer

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeUUID(uuid);
        buf.writeVarLong(uniqueId);
        buf.writeString(name);
        buf.writeString(xuid);
        buf.writeString(platformOnlineId);
        buf.writeIntLE(platform);
        skinData.encode(buf);
        buf.writeBoolean(isTeacher);
        buf.writeBoolean(isHostingPlayer);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        uuid = buf.readUUID();
        uniqueId = buf.readVarLong();
        name = buf.readString();
        xuid = buf.readString();
        platformOnlineId = buf.readString();
        platform = buf.readIntLE();
        skinData = new SerializedSkin();
        skinData.decode(buf);
        isTeacher = buf.readBoolean();
        isHostingPlayer = buf.readBoolean();
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
