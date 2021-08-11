package org.nukkit.raknetty.example;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.bedrock.ProtocolUtil;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ZlibTest {

    static Logger LOGGER = LoggerFactory.getLogger(ZlibTest.class);

    public static void main(String[] args) throws IOException {

        File file;

        file = new File("D:/development/nukkit/login.bin");
        ByteBuf buf1 = Unpooled.wrappedBuffer(Files.readAllBytes(file.toPath()));
        Validate.isTrue(buf1.readByte() == (byte) 0xfe);
        testPacket(buf1);

        file = new File("D:/development/nukkit/sc_handshake.bin");
        ByteBuf buf2 = Unpooled.wrappedBuffer(Files.readAllBytes(file.toPath()));
        buf2.skipBytes(14);
        Validate.isTrue(buf2.readByte() == (byte) 0xfe);
        testPacket(buf2);

    }

    private static void testPacket(ByteBuf buf) {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new JdkZlibDecoder(ZlibWrapper.NONE));
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        long size = VarIntUtil.readUnsignedVarInt(buf);
                        LOGGER.debug("Size: {}", size);
                        LOGGER.debug("Readable: {}", buf.readableBytes());

                        int packetId = buf.getByte(buf.readerIndex());
                        LOGGER.debug("PacketId: {}", packetId);

                        switch (packetId) {
                            case 0x01: //Login
                                handleLoginPacket2(buf);
                                break;
                            case 0x03: //Server To Client Handshake
                                handleServerToClientHandshake(buf);
                                break;
                            default:
                                LOGGER.debug("Unknown");
                        }

                        if (buf.isReadable()) {
                            LOGGER.debug(ByteBufUtil.prettyHexDump(buf));
                        }
                    }
                });
            }
        });
        channel.writeInbound(buf);
        channel.flush();
        channel.close();
    }

    private static ECPublicKey getECPublicKey(String base64) {
        try {
            return (ECPublicKey) KeyFactory
                    .getInstance("EC")
                    .generatePublic(
                            new X509EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleLoginPacket2(ByteBuf buf) {
        LoginPacket packet = new LoginPacket(true);
        packet.decode(buf);

        LOGGER.debug("Protocol: {}", packet.protocolVersion);
        LOGGER.debug("Client key: {}", packet.clientKey);
        LOGGER.debug("Extra data: {}", packet.extraData);
        LOGGER.debug("Skin data: {}", packet.skinData);
    }

    private static void handleServerToClientHandshake(ByteBuf buf) throws
            NoSuchAlgorithmException, InvalidKeySpecException {
        buf.skipBytes(1);
        String jwtStr = ProtocolUtil.readString(buf);
        LOGGER.debug("JWT STR: {}", jwtStr);

        DecodedJWT jwt = JWT.decode(jwtStr);
        String x5u = jwt.getHeaderClaim("x5u").asString();
        String salt = jwt.getClaim("salt").asString();

        PublicKey serverPublicKey = KeyFactory
                .getInstance("EC")
                .generatePublic(
                        new X509EncodedKeySpec(Base64.getDecoder().decode(x5u)));
        LOGGER.debug("Server Public Key: {}", serverPublicKey);
        LOGGER.debug("Salt: {}", salt);
    }
}
