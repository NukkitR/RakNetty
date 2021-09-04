package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.channel.RakServerChannel;

public interface OfflinePingResponse {

    ByteBuf get(RakServerChannel channel);

    public interface Builder<R extends OfflinePingResponse> {
        R build();
    }
}
