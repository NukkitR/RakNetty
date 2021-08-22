package org.nukkit.raknetty.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.compression.ZlibWrapper;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.bedrock.CompressionHandler;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ZlibTest {

    static Logger LOGGER = LoggerFactory.getLogger(ZlibTest.class);

    public static void main(String[] args) throws Exception {


        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) throws Exception {
                ch.pipeline().addLast(new CompressionHandler(ZlibWrapper.NONE));
                ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        ByteBuf buf = (ByteBuf) msg;
                        LOGGER.debug(ByteBufUtil.prettyHexDump(buf));

                        long size = VarIntUtil.readUnsignedVarInt(buf);
                        LOGGER.debug("Size: {}", size);
                        LOGGER.debug("Readable: {}", buf.readableBytes());

                        int packetId = buf.getByte(buf.readerIndex());
                        LOGGER.debug("PacketId: {}", packetId);

                        switch (packetId) {
                            case 0x01: //Login
                                handleLoginPacket2(buf);
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

        File file;
        ByteBuf buf;

        file = new File("D:/development/nukkit/login4.bin");
        buf = Unpooled.wrappedBuffer(Files.readAllBytes(file.toPath()));
        Validate.isTrue(buf.readByte() == (byte) 0xfe);
        channel.writeInbound(buf);

        /*
        file = new File("D:/development/nukkit/sc_handshake2.bin");
        ByteBuf buf2 = Unpooled.wrappedBuffer(Files.readAllBytes(file.toPath()));
        buf2.skipBytes(14);
        Validate.isTrue(buf2.readByte() == (byte) 0xfe);
        testPacket(buf2);*/

        testCompress();
        testDecompress();

    }

    private static void handleLoginPacket2(ByteBuf buf) throws Exception {
        LoginPacket packet = new LoginPacket();
        packet.decode(buf);
        //DecodedJWT jwt = WebTokenUtil.verifySelfSigned(packet.tokens);
        LOGGER.debug("Protocol: {}", packet.protocolVersion);
        LOGGER.debug("Tokens: {}", packet.tokens);
        //LOGGER.debug("Client key: {}", WebTokenUtil.readECPublicKey(jwt.getClaim("identityPublicKey").asString()));
        //LOGGER.debug("Extra data: {}", jwt.getClaim("extraData").asMap());
        LOGGER.debug("Skin JWT: {}", packet.skinJwt);
    }

    private static void testCompress() throws DecoderException {
        LOGGER.debug("JDK: {}", System.getProperty("java.specification.version"));
        byte[] in = Hex.decodeHex("0104");
        Deflater deflater = new Deflater(8, true);
        deflater.setInput(in);
        deflater.finish();
        byte[] out = new byte[512];
        int numBytes = deflater.deflate(out, 0, out.length);
        out = Arrays.copyOf(out, numBytes);
        LOGGER.debug("Num of bytes: {}", numBytes);
        LOGGER.debug("Out: {}", Hex.encodeHexString(out));
    }

    private static void testDecompress() throws DecoderException, DataFormatException {
        byte[] in = Hex.decodeHex("626401");
        Inflater inflater = new Inflater(true);
        inflater.setInput(in);
        byte[] out = new byte[32];
        int numBytes = inflater.inflate(out, 0, out.length);
        out = Arrays.copyOf(out, numBytes);
        LOGGER.debug("Num of bytes: {}", numBytes);
        LOGGER.debug("Out: {}", Hex.encodeHexString(out));
    }

}
