package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LoginPacket implements ClientBedrockPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginPacket.class);

    public int protocolVersion;
    public String tokens;
    public String skinJwt;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.LOGIN;
    }

    @Override
    public void encode(BedrockByteBuf buf) {
        Validate.notNull(tokens, "tokens cannot be empty");
        Validate.notNull(skinJwt, "skin data cannot be empty");
        buf.writeInt(protocolVersion);

        byte[] tokenBytes = tokens.getBytes(StandardCharsets.UTF_8);
        byte[] skinBytes = skinJwt.getBytes(StandardCharsets.UTF_8);

        int payloadSize = 4 + tokenBytes.length + 4 + skinBytes.length;
        buf.writeUnsignedVarInt(payloadSize);
        buf.writeIntLE(tokenBytes.length);
        buf.writeBytes(tokenBytes);
        buf.writeIntLE(skinBytes.length);
        buf.writeBytes(skinBytes);
    }

    @Override
    public void decode(BedrockByteBuf buf) {
        protocolVersion = buf.readInt();
        ByteBuf slice = buf.readSlice(buf.readUnsignedVarInt());
        int len;
        len = slice.readIntLE();
        tokens = slice.toString(slice.readerIndex(), len, StandardCharsets.UTF_8);
        len = slice.readIntLE();
        skinJwt = slice.toString(slice.readerIndex(), len, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("protocolVersion", protocolVersion)
                .append("tokens", tokens)
                .append("skinJwt", skinJwt)
                .toString();
    }
}
