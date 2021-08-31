package org.nukkit.raknetty.handler.codec.bedrock;

import org.nukkit.raknetty.handler.codec.bedrock.packet.*;

public enum PacketIdentifier {
    // see also MinecraftPackets::createPacket

    RESERVED_0,
    LOGIN(
            LoginPacket.class),
    PLAY_STATUS(
            PlayStatusPacket.class),
    SERVER_TO_CLIENT_HANDSHAKE(
            ServerToClientHandshake.class),
    CLIENT_TO_SERVER_HANDSHAKE(
            ClientToServerHandshake.class),
    DISCONNECT(
            DisconnectPacket.class),
    RESOURCE_PACKS_INFO(
            ResourcePacksInfoPacket.class),
    RESOURCE_PACKS_STACK(
            ResourcePacksStackPacket.class
    ),
    RESOURCE_PACK_CLIENT_RESPONSE(
            ResourcePackClientResponsePacket.class),
    TEXT,
    SET_TIME(
            SetTimePacket.class),
    START_GAME,
    ADD_PLAYER,
    ADD_ACTOR,
    REMOVE_ACTOR,
    ADD_ITEM_ACTOR,
    RESERVED_16,
    TAKE_ITEM_ACTOR,
    MOVE_ACTOR_ABSOLUTE,
    MOVE_PLAYER,
    RIDER_JUMP,
    UPDATE_BLOCK,
    ADD_PAINTING,
    TICK_SYNC,
    LEVEL_SOUND_EVENT_OLD,
    LEVEL_EVENT,
    BLOCK_EVENT,
    ACTOR_EVENT,
    MOB_EFFECT,
    UPDATE_ATTRIBUTES,
    INVENTORY_TRANSACTION,
    MOB_EQUIPMENT,
    MOB_ARMOR_EQUIPMENT,
    INTERACT,
    BLOCK_PICK_REQUEST,
    ACTOR_PICK_REQUEST,
    PLAYER_ACTION,
    RESERVED_37,
    HURT_ARMOR,
    SET_ACTOR_DATA,
    SET_ACTOR_MOTION,
    SET_ACTOR_LINK,
    SET_HEALTH,
    SET_SPAWN_POSITION,
    ANIMATE,
    RESPAWN,
    CONTAINER_OPEN,
    CONTAINER_CLOSE,
    PLAYER_HOTBAR,
    INVENTORY_CONTENT,
    INVENTORY_SLOT(
            InventorySlotPacket.class),
    CONTAINER_SET_DATA,
    CRAFTING_DATA,
    CRAFTING_EVENT,
    GUI_DATA_PICK_ITEM,
    ADVENTURE_SETTINGS,
    BLOCK_ACTOR_DATA,
    PLAYER_INPUT,
    LEVEL_CHUNK,
    SET_COMMANDS_ENABLES,
    SET_DIFFICULTY,
    CHANGE_DIMENSION,
    SET_PLAYER_GAME_TYPE,
    PLAYER_LIST(
            PlayerListPacket.class),
    SIMPLE_EVENT(
            SimpleEventPacket.class),
    TELEMETRY_EVENT,
    SPAWN_EXPERIENCE_ORB,
    CLIENTBOUND_MAP_ITEM_DATA,
    MAP_INFO_REQUEST,
    REQUEST_CHUNK_RADIUS,
    CHUNK_RADIUS_UPDATE,
    ITEM_FRAME_DROP_ITEM,
    GAME_RULES_CHANGED,
    CAMERA,
    BOSS_EVENT,
    SHOW_CREDITS,
    AVAILABLE_COMMANDS,
    COMMAND_REQUEST,
    COMMEND_BLOCK_UPDATE,
    COMMAND_OUTPUT,
    UPDATE_TRADE,
    UPDATE_EQUIPMENT,
    RESOURCE_PACK_DATA_INFO(ResourcePackDataInfoPacket.class),
    RESOURCE_PACK_CHUNK_DATA,
    RESOURCE_PACK_CHUNK_REQUEST,
    TRANSFER,
    PLAY_SOUND,
    STOP_SOUND,
    SET_TITLE,
    ADD_BEHAVIOR_TREE,
    STRUCTURE_BLOCK_UPDATE,
    SHOW_STORE_OFFER,
    PURCHASE_RECEIPT,
    PLAYER_SKIN,
    SUB_CLIENT_LOGIN,
    INITIATE_WEB_SOCKET_CONNECTION,
    SET_LAST_HURT_BY,
    BOOK_EDIT,
    NPC_REQUEST,
    PHOTO_TRANSFER,
    MODAL_FORM_REQUEST,
    MODAL_FORM_RESPONSE,
    SERVER_SETTINGS_REQUEST,
    SERVER_SETTINGS_RESPONSE,
    SHOW_PROFILE,
    SET_DEFAULT_GAME_TYPE,
    REMOVE_OBJECTIVE,
    SET_DISPLAY_OBJECTIVE,
    SET_SCORE,
    LAB_TABLE,
    UPDATE_BLOCK_SYNCED,
    MOVE_ACTOR_DELTA,
    SET_SCOREBOARD_IDENTITY,
    SET_LOCAL_PLAYER_AS_INITIALIZED,
    UPDATE_SOFT_ENUM,
    NETWORK_STACK_LATENCY,
    RESERVED_116,
    SCRIPT_CUSTOM_EVENT,
    SPAWN_PARTICLE_EFFECT,
    AVAILABLE_ACTOR_IDENTITIES,
    LEVEL_SOUND_EVENT_V2,
    NETWORK_CHUNK_PUBLISHER_UPDATE,
    BIOME_DEFINITION_LIST,
    LEVEL_SOUND_EVENT,
    LEVEL_EVENT_GENERIC,
    LECTERN_UPDATE,
    RESERVED_126,
    ADD_ENTITY,
    REMOVE_ENTITY,
    CLIENT_CACHE_STATUS(ClientCacheStatusPacket.class),
    ON_SCREEN_TEXTURE_ANIMATION,
    MAP_CREATE_LOCKED_COPY,
    STRUCTURE_TEMPLATE_DATA_EXPORT_REQUEST,
    STRUCTURE_TEMPLATE_DATA_EXPORT_RESPONSE,
    RESERVED_134,
    CLIENT_CACHE_BLOB_STATUS,
    CLIENT_CACHE_MISS_RESPONSE,
    EDUCATION_SETTINGS,
    EMOTE,
    MULTIPLAYER_SETTINGS,
    SETTINGS_COMMAND_PACKET,
    ANVIL_DAMAGE,
    COMPLETED_USING_ITEM,
    NETWORK_SETTINGS(
            NetworkSettingsPacket.class),
    PLAYER_AUTH_INPUT,
    CREATIVE_CONTENT,
    PLAYER_ENCHANT_OPTIONS,
    ITEM_STACK_REQUEST,
    ITEM_STACK_RESPONSE,
    PLAYER_ARMOR_DAMAGE,
    CODE_BUILDER,
    UPDATE_PLAYER_GAME_TYPE,
    EMOTE_LIST,
    POSITION_TRACKING_DB_SERVER_BROADCAST,
    POSITION_TRACKING_DB_SERVER_REQUEST,
    DEBUG_INFO,
    PACKET_VIOLATION_WARNING,
    MOTION_PREDICTION_HINTS,
    ANIMATE_ENTITY,
    CAMERA_SHAKE,
    PLAYER_FOG,
    CORRECT_PLAYER_MOVE_PREDICTION,
    ITEM_COMPONENT,
    FILTER_TEXT,
    CLIENTBOUND_DEBUG_RENDERER,
    SYNC_ACTOR_PROPERTY,
    ADD_VOLUME_ENTITY,
    REMOVE_VOLUME_ENTITY,
    SIMULATION_TYPE,
    NPC_DIALOGUE,
    ;

    private Class<? extends BedrockPacket> clazz = null;

    PacketIdentifier() {
    }

    PacketIdentifier(Class<? extends BedrockPacket> clazz) {
        this.clazz = clazz;
    }

    public BedrockPacket createPacket() {
        if (clazz != null) {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Unable to create packet", e);
            }
        }
        return null;
    }

    public static final PacketIdentifier[] PACKET_IDENTIFIERS = PacketIdentifier.values();
    public static final int NUM_OF_IDENTIFIERS = PACKET_IDENTIFIERS.length;

    public static PacketIdentifier valueOf(int id) {
        if (id < 0 || id >= NUM_OF_IDENTIFIERS) return null;
        return PACKET_IDENTIFIERS[id];
    }
}
