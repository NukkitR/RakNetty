package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;

public interface NetworkHandler extends ChannelInboundHandler {

    enum LoginStatus {
        NO_ACTION,
        HANDSHAKING,
        SUCCESS,
        FAILED
    }

    ChannelFuture loginFuture();

}
