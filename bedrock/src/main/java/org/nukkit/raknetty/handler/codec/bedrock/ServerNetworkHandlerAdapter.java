package org.nukkit.raknetty.handler.codec.bedrock;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ClientToServerHandshake;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.PlayStatusPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.ServerToClientHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.concurrent.ThreadLocalRandom;

public final class ServerNetworkHandlerAdapter extends NetworkHandlerAdapter implements ServerNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerNetworkHandlerAdapter.class);

    public ServerNetworkHandlerAdapter(BedrockChannel channel) {
        super(channel);
        Validate.isTrue(!channel.isClient());
    }

    @Override
    public void dispatch(ChannelHandlerContext ctx, BedrockPacket in) throws Exception {
        if (in instanceof LoginPacket) {
            handle(ctx, (LoginPacket) in);

        } else if (in instanceof ClientToServerHandshake) {
            handle(ctx, (ClientToServerHandshake) in);

        } else {
            super.dispatch(ctx, in);
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, LoginPacket in) throws Exception {
        if (loginStatus != LoginStatus.NO_ACTION) {
            return;
        }

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
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ClientToServerHandshake in) {
        if (loginStatus != LoginStatus.HANDSHAKING) return;

        loginStatus = LoginStatus.SUCCESS;
        loginFuture.markAsSuccess();
        LOGGER.debug("C->S Handshake");

        PlayStatusPacket out = new PlayStatusPacket();
        out.status = PlayStatusPacket.PlayStatus.LOGIN_SUCCESS;
        ctx.write(out);
    }
}
