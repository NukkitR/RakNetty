package org.nukkit.raknetty.handler.codec.bedrock;

import org.nukkit.raknetty.handler.codec.bedrock.packet.*;

public interface ClientNetworkHandler extends NetworkHandler {

    void handle(PlayStatusPacket in);

    void handle(ServerToClientHandshake in);

    void handle(DisconnectPacket in);

    void handle(ResourcePacksInfoPacket in);

    void handle(ResourcePacksStackPacket in);

    void handle(ResourcePackDataInfoPacket in);

    void handle(NetworkSettingsPacket in);

    void handle(PlayerListPacket in);

    void handle(SetTimePacket in);

    void handle(SimpleEventPacket in);

}
