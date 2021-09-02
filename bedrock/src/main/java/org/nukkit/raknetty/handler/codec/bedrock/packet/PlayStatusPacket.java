package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

public class PlayStatusPacket implements ServerBedrockPacket {

    public Status status;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.PLAY_STATUS;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        int index = buf.readInt();
        Validate.isTrue(index >= 0 && index < Status.NUM_OF_STATUSES, "Unknown play status");
        status = Status.PLAY_STATUSES[index];
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("status", status)
                .toString();
    }

    public enum Status {
        LOGIN_SUCCESS(true),
        OUTDATED_CLIENT(false),
        OUTDATED_SERVER(false),
        PLAYER_SPAWN(true),
        INVALID_TENANT(false),
        EDITION_MISMATCH_VANILLA_TO_EDU(false),
        EDITION_MISMATCH_EDU_TO_VANILLA(false),
        SERVER_FULL(false);

        boolean success;

        Status(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return this.success;
        }

        public static final Status[] PLAY_STATUSES = Status.values();
        public static final int NUM_OF_STATUSES = PLAY_STATUSES.length;
    }
}
