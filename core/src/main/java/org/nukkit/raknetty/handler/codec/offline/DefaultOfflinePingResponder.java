package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;

public class DefaultOfflinePingResponder implements OfflinePingResponder {

    protected final ByteBuf buf;

    public DefaultOfflinePingResponder() {
        buf = Unpooled.buffer();
        buf.writeCharSequence("Offline Ping Data", StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuf response() {
        return buf;
    }
}
