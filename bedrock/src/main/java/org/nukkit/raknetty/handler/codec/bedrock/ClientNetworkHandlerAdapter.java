package org.nukkit.raknetty.handler.codec.bedrock;

import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.LoginStatus;
import org.nukkit.raknetty.handler.codec.bedrock.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class ClientNetworkHandlerAdapter extends AbstractNetworkHandler implements ClientNetworkHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNetworkHandlerAdapter.class);

    public ClientNetworkHandlerAdapter(BedrockChannel channel) {
        super(channel);
        Validate.isTrue(channel.isClient());
    }

    @Override
    protected void registerPackets() {
        registerPacket(PlayStatusPacket.class, this::handle);
        registerPacket(ServerToClientHandshake.class, this::handle);
        registerPacket(DisconnectPacket.class, this::handle);
        registerPacket(ResourcePacksInfoPacket.class, this::handle);
        registerPacket(ResourcePacksStackPacket.class, this::handle);
        registerPacket(ResourcePackDataInfoPacket.class, this::handle);
        registerPacket(NetworkSettingsPacket.class, this::handle);
        registerPacket(InventorySlotPacket.class, this::handle);
        registerPacket(SimpleEventPacket.class, this::handle);
        registerPacket(SetTimePacket.class, this::handle);
        registerPacket(PlayerListPacket.class, this::handle);
    }

    @Override
    public void handle(PlayStatusPacket in) {
        LOGGER.debug("{}", in);
        PlayStatusPacket.Status status = in.status;

        if (status == PlayStatusPacket.Status.LOGIN_SUCCESS && loginStatus == LoginStatus.HANDSHAKING) {
            loginStatus = LoginStatus.PLAY;
            loginFuture.markAsSuccess();

        } else if (status.isSuccess()) {
            // could be PLAYER_SPAWN
            ctx.fireChannelRead(in);

        } else {
            // login is not success
            loginFuture.markAsFailed(status.toString());
            channel.disconnect();
        }
    }

    @Override
    public void handle(ServerToClientHandshake in) {
        if (loginStatus != LoginStatus.LOGIN) return;

        //LOGGER.debug("S->C Handshake");
        //LOGGER.debug("Server Public Key: {}", packet.serverPublicKey);
        //LOGGER.debug("Salt: {}", Base64.getEncoder().encodeToString(in.salt));

        // enable the encryption before the handshake so that the server will ensure the encryption is successfully established.
        try {
            channel.enableEncryption(in.serverPublicKey, in.salt);
        } catch (Exception e) {
            LOGGER.warn("Unable to enable encryption", e);
            return;
        }

        ClientToServerHandshake out = new ClientToServerHandshake();
        ctx.write(out);

        loginStatus = LoginStatus.HANDSHAKING;
    }

    @Override
    public void handle(DisconnectPacket in) {
        loginFuture.markAsFailed(in.reason.toString());
        LOGGER.debug("Disconnected from server: {}", in.reason);
        channel.disconnect();
    }

    @Override
    public void handle(ResourcePacksInfoPacket in) {
        LOGGER.debug("{}", in);

        ClientCacheStatusPacket status = new ClientCacheStatusPacket();
        status.isEnabled = true;
        ctx.write(status);

        ResourcePackClientResponsePacket out = new ResourcePackClientResponsePacket();
        out.status = ResourcePackClientResponsePacket.Status.DOWNLOAD_FINISHED;
        out.packIdVersions = new ArrayList<>();
        out.packIdVersions.addAll(
                in.behaviorPacksInfo.stream()
                        .map(info -> info.packId + "_" + info.version)
                        .collect(Collectors.toList()));
        out.packIdVersions.addAll(
                in.resourcePacksInfo.stream()
                        .map(info -> info.packId + "_" + info.version)
                        .collect(Collectors.toList()));

        ctx.write(out);
    }

    @Override
    public void handle(ResourcePacksStackPacket in) {
        LOGGER.debug("{}", in);

        ResourcePackClientResponsePacket out = new ResourcePackClientResponsePacket();
        out.status = ResourcePackClientResponsePacket.Status.READY;
        out.packIdVersions = new ArrayList<>();
        out.packIdVersions.addAll(in.behaviorPackStack.stream()
                .map(stack -> stack.getPackId() + "_" + stack.getVersion())
                .collect(Collectors.toList()));
        out.packIdVersions.addAll(in.resourcePackStack.stream()
                .map(stack -> stack.getPackId() + "_" + stack.getVersion())
                .collect(Collectors.toList()));

        ctx.write(out);
    }

    @Override
    public void handle(SimpleEventPacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(ResourcePackDataInfoPacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(NetworkSettingsPacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(PlayerListPacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(SetTimePacket in) {
        LOGGER.debug("{}", in);
    }

    @Override
    public void handle(InventorySlotPacket in) {
        LOGGER.debug("{}", in);
    }
}
