package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelHandlerContext;
import org.nukkit.raknetty.handler.codec.bedrock.packet.DisconnectPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.NetworkSettingsPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.PlayStatusPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ServerToClientHandshake;

public interface ClientNetworkHandler extends NetworkHandler {

    void handle(ChannelHandlerContext ctx, PlayStatusPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, ServerToClientHandshake in) throws Exception;

    void handle(ChannelHandlerContext ctx, DisconnectPacket in) throws Exception;

    void handle(ChannelHandlerContext ctx, NetworkSettingsPacket in) throws Exception;

}
