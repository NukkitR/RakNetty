package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

public class DisconnectionNotification implements ReliabilityMessage {

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_DISCONNECTION_NOTIFICATION;
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {

    }

    @Override
    public void encode(ByteBuf buf) throws Exception {

    }
}
