package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedrockPacketUtil {

    public static final int PROTOCOL_NETWORK_VERSION = 448;

    private static final Logger LOGGER = LoggerFactory.getLogger(BedrockPacketUtil.class);

    public static byte getByte(ByteBuf buf) {
        return buf.getByte(buf.readerIndex());
    }

    public static int getUnsignedByte(ByteBuf buf) {
        return buf.getUnsignedByte(buf.readerIndex());
    }

    public static String readString(ByteBuf buf) {
        buf.markReaderIndex();
        int len = (int) VarIntUtil.readUnsignedVarInt(buf);
        if (len <= 0 || len > buf.readableBytes()) {
            buf.resetReaderIndex();
            return null;
        }

        byte[] dst = new byte[len];
        buf.readBytes(dst);
        return new String(dst, CharsetUtil.UTF_8);
    }

    public static void writeString(ByteBuf buf, String str) {
        VarIntUtil.writeUnsignedVarInt(buf, str.length());
        buf.writeBytes(str.getBytes(CharsetUtil.UTF_8));
    }

}
