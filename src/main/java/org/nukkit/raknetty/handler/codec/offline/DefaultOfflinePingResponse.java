package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.nukkit.raknetty.channel.RakServerChannel;

import java.nio.charset.StandardCharsets;

public class DefaultOfflinePingResponse implements OfflinePingResponse {

    protected final ByteBuf offlineData;

    public DefaultOfflinePingResponse(String message) {
        this.offlineData = Unpooled.buffer();
        this.offlineData.writeCharSequence(message, StandardCharsets.UTF_8);
    }

    @Override
    public ByteBuf get(RakServerChannel channel) {
        return offlineData;
    }
}
