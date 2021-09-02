package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInboundHandler;
import org.nukkit.raknetty.handler.codec.bedrock.packet.InventorySlotPacket;

public interface NetworkHandler extends ChannelInboundHandler {

    ChannelFuture loginFuture();

    void handle(InventorySlotPacket in);

}
