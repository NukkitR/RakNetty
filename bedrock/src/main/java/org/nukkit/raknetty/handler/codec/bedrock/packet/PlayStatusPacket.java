package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.PlayStatus;

public class PlayStatusPacket extends AbstractBedrockPacket implements ServerBedrockPacket {

    public PlayStatus status;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.PLAY_STATUS;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeInt(status.ordinal());
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        int index = buf.readInt();
        Validate.isTrue(index >= 0 && index < PlayStatus.NUM_OF_STATUSES, "Unknown play status");
        status = PlayStatus.PLAY_STATUSES[index];
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("status", status)
                .toString();
    }
}
