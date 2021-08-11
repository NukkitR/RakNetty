package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.nio.BedrockChannel;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchPacketHandler extends ChannelDuplexHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(BatchPacketHandler.class);

    enum LoginStatus {
        NO_ACTION,
        REQUESTED_LOGIN,        // client send LoginPacket
        HANDLING_LOGIN_REQUEST, // server receive LoginPacket
        HANDSHAKING,            // server send ServerToClientHandshakePacket
        LOGGED_IN,              // client send or server receive ClientToServerHandshakePacket
    }

    private BedrockChannel channel;
    private LoginStatus loginStatus = LoginStatus.NO_ACTION;

    public BatchPacketHandler(BedrockChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        while (buf.isReadable()) {
            long size = VarIntUtil.readUnsignedVarInt(buf);
            LOGGER.debug("Size: {}", size);
            LOGGER.debug("Readable: {}", buf.readableBytes());

            int packetId = buf.getByte(buf.readerIndex());
            LOGGER.debug("PacketId: {}", packetId);

            switch (packetId) {
                case 0x01: //Login
                    handleLoginPacket(ctx, buf);
                    break;
                case 0x03: //Server To Client Handshake
                    //handleServerToClientHandshake(buf);
                    break;
                default:
                    LOGGER.debug("Unknown");
            }

            if (buf.isReadable()) {
                LOGGER.debug(ByteBufUtil.prettyHexDump(buf));
            }
        }

        ReferenceCountUtil.release(buf);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Validate.isTrue(loginStatus == LoginStatus.NO_ACTION, "handler has already been initialised.");
        super.handlerAdded(ctx);
    }

    private void handleLoginPacket(ChannelHandlerContext ctx, ByteBuf buf) {
        LoginPacket packet = new LoginPacket(channel.config().isOnlineAuthenticationEnabled());
        packet.decode(buf);
        LOGGER.debug("Protocol: {}", packet.protocolVersion);
        LOGGER.debug("Client key: {}", packet.clientKey);
        LOGGER.debug("Extra data: {}", packet.extraData);
        LOGGER.debug("Skin data: {}", packet.skinData);
    }


}
