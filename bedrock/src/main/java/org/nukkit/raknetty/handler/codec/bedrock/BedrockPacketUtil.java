package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.nukkit.raknetty.util.BinaryUtil;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedrockPacketUtil extends BinaryUtil {

    public static final int PROTOCOL_NETWORK_VERSION = 448;

    private static final Logger LOGGER = LoggerFactory.getLogger(BedrockPacketUtil.class);

    public static String readString(ByteBuf buf) {
        return BinaryUtil.readString(buf,
                () -> VarIntUtil.readUnsignedVarInt(buf));
    }

    public static void writeString(ByteBuf buf, String str) {
        BinaryUtil.writeString(buf, str,
                strLen -> VarIntUtil.writeUnsignedVarInt(buf, strLen));
        buf.writeBytes(str.getBytes(CharsetUtil.UTF_8));
    }

}
