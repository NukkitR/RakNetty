package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;

public interface OfflinePingResponder {
    ByteBuf response();
}
