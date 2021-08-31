package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackInfoData;
import org.nukkit.raknetty.handler.codec.bedrock.data.PlayStatus;
import org.nukkit.raknetty.handler.codec.bedrock.data.ResourcePackClientStatus;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientNetworkHandlerAdapter extends NetworkHandlerAdapter implements ClientNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNetworkHandlerAdapter.class);

    public ClientNetworkHandlerAdapter(BedrockChannel channel) {
        super(channel);
        Validate.isTrue(channel.isClient());

        registerPacketEvent(PlayStatusPacket.class, this::handle);
        registerPacketEvent(ServerToClientHandshake.class, this::handle);
        registerPacketEvent(DisconnectPacket.class, this::handle);
        registerPacketEvent(ResourcePacksInfoPacket.class, this::handle);
        registerPacketEvent(ResourcePacksStackPacket.class, this::handle);
        registerPacketEvent(ResourcePackDataInfoPacket.class, this::handle);
        registerPacketEvent(NetworkSettingsPacket.class, this::handle);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, PlayStatusPacket in) throws Exception {
        LOGGER.debug("{}", in);
        PlayStatus status = in.status;

        if (status == PlayStatus.LOGIN_SUCCESS && loginStatus == LoginStatus.HANDSHAKING) {
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
    public void handle(ChannelHandlerContext ctx, ResourcePacksInfoPacket in) throws Exception {
        LOGGER.debug("{}", in);

        ClientCacheStatusPacket status = new ClientCacheStatusPacket();
        status.isEnabled = true;
        ctx.write(status);

        ResourcePackClientResponsePacket out = new ResourcePackClientResponsePacket();
        out.status = ResourcePackClientStatus.DOWNLOAD_FINISHED;
        out.packIdVersions = new ArrayList<>();
        out.packIdVersions.addAll(
                in.behaviorPacksInfo.stream()
                        .map(PackInfoData.Behavior::toIdVersion)
                        .collect(Collectors.toList()));
        out.packIdVersions.addAll(
                in.resourcePacksInfo.stream()
                        .map(PackInfoData.Resource::toIdVersion)
                        .collect(Collectors.toList()));

        ctx.write(out);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ResourcePacksStackPacket in) throws Exception {
        LOGGER.debug("{}", in);

        ResourcePackClientResponsePacket out = new ResourcePackClientResponsePacket();
        out.status = ResourcePackClientStatus.READY;
        out.packIdVersions = new ArrayList<>();
        out.packIdVersions.addAll(in.behaviorPacksInfo);
        out.packIdVersions.addAll(in.resourcePacksInfo);

        ctx.write(out);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, ResourcePackDataInfoPacket in) throws Exception {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, NetworkSettingsPacket in) throws Exception {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, PlayerListPacket in) throws Exception {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(ChannelHandlerContext ctx, SetTimePacket in) throws Exception {
        LOGGER.debug("{}", in);
    }
}
