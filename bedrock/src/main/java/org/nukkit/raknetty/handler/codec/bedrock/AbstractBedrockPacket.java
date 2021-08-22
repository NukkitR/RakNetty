package org.nukkit.raknetty.handler.codec.bedrock;

import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class AbstractBedrockPacket implements BedrockPacket {
    int senderId = 0;
    int clientId = 0;

    @Override
    public int getClientId() {
        return clientId;
    }

    @Override
    public int getSenderId() {
        return senderId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("packetId", getId())
                .toString();
    }
}
