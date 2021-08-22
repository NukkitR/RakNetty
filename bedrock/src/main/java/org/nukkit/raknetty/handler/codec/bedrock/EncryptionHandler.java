package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

public final class EncryptionHandler extends ChannelDuplexHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionHandler.class);
    private static final byte BATCH_PACKET_ID = (byte) 0xfe;

    private final BedrockChannel channel;
    private final Cipher encryption;
    private long writeCounter = 0;
    private final Cipher decryption;
    private long readCounter = 0;
    private byte[] sharedSecret;

    public EncryptionHandler(BedrockChannel channel) {
        this.channel = channel;

        try {
            encryption = Cipher.getInstance("AES/CTR/NoPadding");
            decryption = Cipher.getInstance("AES/CTR/NoPadding");

        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new IllegalStateException("Failed to create encryption handler", e);
        }
    }

    public void init(byte[] sharedSecret) throws InvalidAlgorithmParameterException, InvalidKeyException {
        Validate.isTrue(!channel.isEncrypted());
        Validate.notNull(sharedSecret, "shared secret must not be null");
        this.sharedSecret = sharedSecret;
        SecretKey secretKey = new SecretKeySpec(sharedSecret, "AES");

        ByteBuf buf = channel.alloc().buffer(16, 16);
        buf.writeBytes(sharedSecret, 0, 12);
        buf.writeInt(2);

        byte[] nonceAndCounter = new byte[16];
        buf.readBytes(nonceAndCounter);
        ReferenceCountUtil.release(buf);

        encryption.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(nonceAndCounter));
        decryption.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(nonceAndCounter));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte packetId = BedrockPacketUtil.getByte(buf);

        boolean release = true;

        try {
            if (packetId == BATCH_PACKET_ID) {
                buf.skipBytes(1);

                if (channel.isEncrypted()) {
                    //LOGGER.debug("Encrypted packet: {}", ByteBufUtil.prettyHexDump(buf));
                    ByteBuf decrypted = doCipher(ctx, decryption, buf);
                    //LOGGER.debug("Decrypted packet: {}", ByteBufUtil.prettyHexDump(decrypted));

                    int readableBytes = decrypted.readableBytes();
                    ByteBuf plainText = decrypted.slice(0, readableBytes - 8);

                    byte[] actualMac = new byte[8];
                    decrypted.getBytes(readableBytes - 8, actualMac);
                    byte[] expectMac = generateMAC(plainText, readCounter, sharedSecret);

                    //LOGGER.debug("Expect HMAC: {}", Hex.encodeHexString(expectMac));
                    //LOGGER.debug("Actual HMAC: {}", Hex.encodeHexString(actualMac));
                    Validate.isTrue(Arrays.equals(actualMac, expectMac),
                            "Expect: %s, Actual: %s", Hex.encodeHexString(expectMac), Hex.encodeHexString(actualMac));
                    readCounter++;
                    // pass the decrypted packet to inflater
                    ctx.fireChannelRead(plainText);

                } else {
                    // not encrypted, pass the packet to inflater
                    release = false;
                    ctx.fireChannelRead(buf);
                }
            } else {
                LOGGER.debug("Unhandled: {}", packetId);
            }

        } finally {
            if (release)
                ReferenceCountUtil.release(buf);
        }

    }

    private ByteBuf doCipher(ChannelHandlerContext ctx, Cipher cipher, ByteBuf in) throws ShortBufferException {

        byte[] inArray;
        int readableBytes = in.readableBytes();
        int inputOffset;

        if (in.hasArray()) {
            inArray = in.array();
            inputOffset = in.arrayOffset() + in.readerIndex();
        } else {
            inArray = new byte[readableBytes];
            in.readBytes(inArray);
            inputOffset = 0;
        }

        ByteBuf out = ctx.alloc().heapBuffer(readableBytes);
        byte[] outArray = out.array();
        int writerIndex = out.writerIndex();
        int outOffset = out.arrayOffset() + writerIndex;

        int outputLength = cipher.update(inArray, inputOffset, readableBytes, outArray, outOffset);
        out.writerIndex(writerIndex + outputLength);
        return out;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf body = (ByteBuf) msg;

        if (channel.isEncrypted()) {
            ByteBuf plainText = ctx.alloc().heapBuffer(body.readableBytes() + 8);
            plainText.writeBytes(body, body.readerIndex(), body.readableBytes());
            plainText.writeBytes(generateMAC(body, writeCounter, sharedSecret));
            //LOGGER.debug("plaintext: {}", ByteBufUtil.prettyHexDump(plainText));
            //LOGGER.debug("Secret: {}", Hex.encodeHexString(sharedSecret));
            body = doCipher(ctx, encryption, plainText);
            ReferenceCountUtil.release(msg);
            writeCounter++;
        }

        ByteBuf header = ctx.alloc().buffer(1, 1);
        header.writeByte(BATCH_PACKET_ID);

        CompositeByteBuf composite = ctx.alloc().compositeBuffer(2);
        composite.addComponents(true, header, body);

        channel.send(composite, PacketPriority.HIGH_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);
    }

    public byte[] generateMAC(ByteBuf plainText, long counter, byte[] secret) throws DigestException {
        int capacity = 8 + plainText.readableBytes() + secret.length;
        ByteBuf buf = channel.alloc().heapBuffer(capacity);
        byte[] array = buf.array();
        int offset = buf.arrayOffset() + buf.readerIndex();

        buf.writeLongLE(counter);
        buf.writeBytes(plainText, plainText.readerIndex(), plainText.readableBytes());
        buf.writeBytes(secret);

        MessageDigest sha256 = DigestUtils.getSha256Digest();
        sha256.update(array, offset, buf.readableBytes());

        try {
            return Arrays.copyOf(sha256.digest(), 8);
        } finally {
            ReferenceCountUtil.release(buf);
        }
    }
}
