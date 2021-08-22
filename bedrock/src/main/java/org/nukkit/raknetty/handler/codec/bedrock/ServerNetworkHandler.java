package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ClientToServerHandshake;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;

public interface ServerNetworkHandler extends NetworkHandler {

    void handle(ChannelHandlerContext ctx, LoginPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ClientToServerHandshake in) throws Exception;

}
