package org.nukkit.raknetty.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtTest {

    static final Logger LOGGER = LoggerFactory.getLogger(JwtTest.class);

    public static void main(String[] args) throws DecoderException {
        ByteBuf buf = Unpooled.buffer();
        VarIntUtil.writeUnsignedVarInt(buf, 0xff);
        System.out.println(ByteBufUtil.prettyHexDump(buf));

        buf = Unpooled.wrappedBuffer(Hex.decodeHex("ea60"));
        int uint = (int) VarIntUtil.readUnsignedVarInt(buf);
        System.out.println(uint);
    }
}
