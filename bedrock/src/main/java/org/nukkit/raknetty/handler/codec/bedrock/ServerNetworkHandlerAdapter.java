package org.nukkit.raknetty.handler.codec.bedrock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginStatus;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.ThreadLocalRandom;

public class ServerNetworkHandlerAdapter extends AbstractNetworkHandler implements ServerNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerNetworkHandlerAdapter.class);

    public ServerNetworkHandlerAdapter(BedrockChannel channel) {
        super(channel);
        Validate.isTrue(!channel.isClient());
    }

    @Override
    protected void registerPackets() {

    }

    @Override
    public void handle(LoginPacket in) {
        if (loginStatus != LoginStatus.LOGIN) {
            return;
        }

        try {
            Jws<Claims> jws;
            if (channel.config().isOnlineAuthenticationEnabled()) {
                jws = WebTokenUtil.verifyOnline(in.tokens);
            } else {
                jws = WebTokenUtil.verifySelfSigned(in.tokens);
            }

            // TODO: disconnect() if not authenticated.

            ECPublicKey clientPublicKey = WebTokenUtil.readECPublicKey(
                    jws.getBody().get("identityPublicKey", String.class));

            LOGGER.debug("Login");
            //LOGGER.debug("Protocol: {}", packet.protocolVersion);
            //LOGGER.debug("Client key: {}", clientPublicKey);
            //LOGGER.debug("Extra data: {}", jwt.getClaim("extraData").asMap());
            //LOGGER.debug("Skin data: {}", packet.skinJwt);

            byte[] salt = new byte[16];
            ThreadLocalRandom.current().nextBytes(salt);

            ServerToClientHandshake out = new ServerToClientHandshake();
            out.serverPrivateKey = (ECPrivateKey) channel.localPrivateKey();
            out.serverPublicKey = (ECPublicKey) channel.localPublicKey();
            out.salt = salt;
            ctx.write(out);

            // enable the encryption after the handshake is sent, so that it would not be encrypted.
            channel.enableEncryption(clientPublicKey, salt);
            loginStatus = LoginStatus.HANDSHAKING;
        } catch (Exception e) {
            LOGGER.warn("Failed to handle LoginPacket", e);
        }
    }

    @Override
    public void handle(ClientToServerHandshake in) {
        if (loginStatus != LoginStatus.HANDSHAKING) return;

        loginStatus = LoginStatus.PLAY;
        loginFuture.markAsSuccess();
        LOGGER.debug("C->S Handshake");

        PlayStatusPacket out = new PlayStatusPacket();
        out.status = PlayStatusPacket.Status.LOGIN_SUCCESS;
        ctx.write(out);
    }

    @Override
    public void handle(ClientCacheStatusPacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(ResourcePackClientResponsePacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(InventorySlotPacket in) {
        LOGGER.debug("{}", in);
    }
}
