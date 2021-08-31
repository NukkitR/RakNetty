package org.nukkit.raknetty.handler.codec.bedrock.data;

public enum PlayStatus {
    LOGIN_SUCCESS(true),
    OUTDATED_CLIENT(false),
    OUTDATED_SERVER(false),
    PLAYER_SPAWN(true),
    INVALID_TENANT(false),
    EDITION_MISMATCH_VANILLA_TO_EDU(false),
    EDITION_MISMATCH_EDU_TO_VANILLA(false),
    SERVER_FULL(false);

    boolean success;

    PlayStatus(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return this.success;
    }

    public static final PlayStatus[] PLAY_STATUSES = PlayStatus.values();
    public static final int NUM_OF_STATUSES = PLAY_STATUSES.length;
}