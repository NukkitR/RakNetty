package org.nukkit.raknetty.handler.codec.bedrock;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DisconnectReason {
    DISCONNECTED("disconnected"),
    EDITION_MISMATCH_EDU_TO_VANILLA("editionMismatchEduToVanilla"),
    EDITION_MISMATCH_VANILLA_TO_EDU("editionMismatchVanillaToEdu"),
    HOST_SUSPENDED("hostSuspended"),
    INVALID_PLAYER("invalidPlayer"),
    INVALID_SKIN("invalidSkin"),
    INVALID_TENANT("invalidTenant"),
    LOGGED_IN_OTHER_LOCATION("loggedinOtherLocation"),
    MULTIPLAYER_DISABLED("multiplayerDisabled"),
    NO_REASON("noReason"),
    NOT_ALLOWED("notAllowed"),
    NOT_AUTHENTICATED("notAuthenticated"),
    OUTDATED_CLIENT("outdatedClient"),
    OUTDATED_SERVER("outdatedServer"),
    SERVER_FULL("serverFull"),
    SERVER_ID_CONFLICT("serverIdConflict"),
    TIMEOUT("timeout"),
    UNEXPECTED_PACKET("unexpectedPacket"),
    UNKNOWN_PACKET("unknownPacket"),
    WORLD_CORRUPTION("worldCorruption");

    private static final Map<String, DisconnectReason> REASONS =
            Arrays.stream(DisconnectReason.values()).collect(
                    Collectors.toMap(reason -> reason.id, reason -> reason));

    private final String id;

    DisconnectReason(String id) {
        this.id = "disconnectionScreen." + id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return getId();
    }

    public static DisconnectReason findById(String id) {
        return REASONS.get(id);
    }


}
