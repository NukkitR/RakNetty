package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import org.nukkit.raknetty.handler.codec.bedrock.packet.InventorySlotPacket;

public interface NetworkHandler extends ChannelInboundHandler {

    enum LoginStatus {
        NO_ACTION,
        HANDSHAKING,
        SUCCESS,
        FAILED
    }

    ChannelFuture loginFuture();

    void handle(ChannelHandlerContext ctx, InventorySlotPacket in) throws Exception;

}
