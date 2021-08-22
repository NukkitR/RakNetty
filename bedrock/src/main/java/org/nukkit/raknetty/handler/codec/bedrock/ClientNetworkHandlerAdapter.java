package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ClientNetworkHandlerAdapter extends NetworkHandlerAdapter implements ClientNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNetworkHandlerAdapter.class);

    public ClientNetworkHandlerAdapter(BedrockChannel channel) {
        super(channel);
        Validate.isTrue(channel.isClient());
    }

    @Override
    public void dispatch(ChannelHandlerContext ctx, BedrockPacket in) throws Exception {
        if (in instanceof PlayStatusPacket) {
            handle(ctx, (PlayStatusPacket) in);

        } else if (in instanceof ServerToClientHandshake) {
            handle(ctx, (ServerToClientHandshake) in);

        } else if (in instanceof DisconnectPacket) {
            handle(ctx, (DisconnectPacket) in);

        } else if (in instanceof NetworkSettingsPacket) {
            handle(ctx, (NetworkSettingsPacket) in);

        } else {
            super.dispatch(ctx, in);
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, PlayStatusPacket in) throws Exception {

        PlayStatusPacket.PlayStatus status = in.status;
        LOGGER.debug("Play status: {}", status);

        if (status == PlayStatusPacket.PlayStatus.LOGIN_SUCCESS && loginStatus == LoginStatus.HANDSHAKING) {
            loginStatus = LoginStatus.SUCCESS;
            loginFuture.markAsSuccess();

        } else if (status.isSuccess()) {
            // could be PLAYER_SPAWN
            ctx.fireChannelRead(in);

        } else {
            // login is not success
            loginStatus = LoginStatus.FAILED;
            loginFuture.markAsFailed(status.toString());
            channel.disconnect();
        }
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ServerToClientHandshake in) throws Exception {
        if (loginStatus != LoginStatus.NO_ACTION) return;

        //LOGGER.debug("S->C Handshake");
        //LOGGER.debug("Server Public Key: {}", packet.serverPublicKey);
        //LOGGER.debug("Salt: {}", Base64.getEncoder().encodeToString(in.salt));

        // enable the encryption before the handshake so that the server will ensure the encryption is successfully established.
        channel.enableEncryption(in.serverPublicKey, in.salt);
        ClientToServerHandshake out = new ClientToServerHandshake();
        ctx.write(out);

        loginStatus = LoginStatus.HANDSHAKING;
    }

    @Override
    public void handle(ChannelHandlerContext ctx, DisconnectPacket in) throws Exception {
        loginFuture.markAsFailed(in.reason.toString());
        LOGGER.debug("Disconnected from server: {}", in.reason);
        channel.disconnect();
    }

    @Override
    public void handle(ChannelHandlerContext ctx, NetworkSettingsPacket in) throws Exception {
        LOGGER.debug("Network settings: {}", in);
    }
}
