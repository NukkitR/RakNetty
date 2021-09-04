package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.nukkit.raknetty.channel.RakServerChannel;

import java.nio.charset.StandardCharsets;

public class DefaultOfflinePingResponse implements OfflinePingResponse {

    protected final ByteBuf offlineData;

    protected DefaultOfflinePingResponse(ByteBuf offlineData) {
        this.offlineData = offlineData;
    }

    @Override
    public ByteBuf get(RakServerChannel channel) {
        return offlineData;
    }

    public static class Builder implements OfflinePingResponse.Builder<DefaultOfflinePingResponse> {
        private String message;

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        @Override
        public DefaultOfflinePingResponse build() {
            ByteBuf buf = Unpooled.buffer();
            buf.writeCharSequence(message, StandardCharsets.UTF_8);
            return new DefaultOfflinePingResponse(buf);
        }
    }
}
