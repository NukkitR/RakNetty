package org.nukkit.raknetty.handler.codec.bedrock;

import org.nukkit.raknetty.handler.codec.bedrock.packet.ClientCacheStatusPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ClientToServerHandshake;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ResourcePackClientResponsePacket;

public interface ServerNetworkHandler extends NetworkHandler {

    void handle(LoginPacket in);

    void handle(ClientToServerHandshake in);

    void handle(ResourcePackClientResponsePacket in);

    void handle(ClientCacheStatusPacket in);

}
