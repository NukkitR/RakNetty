package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;

public interface ClientNetworkHandler extends NetworkHandler {

    void handle(ChannelHandlerContext ctx, PlayStatusPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ServerToClientHandshake in) throws Exception;

    void handle(ChannelHandlerContext ctx, DisconnectPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ResourcePacksInfoPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ResourcePacksStackPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ResourcePackDataInfoPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, NetworkSettingsPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, PlayerListPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, SetTimePacket in) throws Exception;

}
