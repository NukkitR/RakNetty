package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.nukkit.raknetty.util.VarIntUtil;

public class ProtocolUtil {

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
