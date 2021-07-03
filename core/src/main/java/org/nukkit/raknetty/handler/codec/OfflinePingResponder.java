package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;

public interface OfflinePingResponder {
    ByteBuf response();
}
