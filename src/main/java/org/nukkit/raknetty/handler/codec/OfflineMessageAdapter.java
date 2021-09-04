package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;

public abstract class OfflineMessageAdapter implements OfflineMessage {

    @Override
    public void encode(ByteBuf buf) throws Exception {

    }

    @Override
    public void decode(ByteBuf buf) throws Exception {

    }
}
