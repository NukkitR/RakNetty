package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class StartGamePacket implements ServerBedrockPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartGamePacket.class);

    public long actorUniqueId;
    public long actorRuntimeId;
    //public GameType gameType;
    //public Vector3f position;
    //public Vector2f rotation;
    //public LevelSettings levelSettings;
    public String levelId;
    public String levelName;
    public UUID contentIdentity = new UUID(0, 0);
    public boolean unknown = false; // always false
    //public PlayerMovementSettings movementSettings;
    public long currentTick;
    public int enchantmentSeed;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.START_GAME;
    }

    @Override
    public void encode(BedrockByteBuf buf) {

    }

    @Override
    public void decode(BedrockByteBuf buf) {
        // varlong +131 actor unique id
        // unsigned varlong +132 runtime id
        // varint +266 gametype
        // float +267 vec3[0] position
        // float +268 vec3[1]
        // float +269 vec3[2]
        // float +270 vec2[0] roation
        // float +271 vec2[1]
        // levelSetting +48
        // string +1088 levelId
        // string +1120 levelName
        // ContentIdentity string +1152 uuid(0, 0)
        // bool +1176 unknown always false
        // PlayerMovementSettings +1180
        // - varint +1180 = PlayerMovementSetting+0 movement type
        // - varint +1184 = PlayerMovementSetting+64 rewindFrames
        // - bool   +1188 serverAuthoritativeBlockBreaking = PlayerMovementSetting+88
        // unsigned long +149 currentTick
        // varint +300 enchantmentSeed
        // array[] pair<string, CompoundTag> +164 unsigned varint
        // - string name
        // - CompoundTag Tag::writeNamedTag
        // array[] itemData +1248 unsigned varint
        // - string ItemData+24 name
        // - unsigned short ItemData+48 runtimeId
        // - bool ItemData+50 seems to always false
        // string +1208 +1224 uuid
        // bool +1272 enable-item-stack-net-manager-deprecated
        // string +1280 gameVersion
    }
}
