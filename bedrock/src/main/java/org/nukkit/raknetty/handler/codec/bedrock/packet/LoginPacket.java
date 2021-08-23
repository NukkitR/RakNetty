package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class LoginPacket extends AbstractBedrockPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginPacket.class);

    public int protocolVersion;
    public String tokens;
    public String skinJwt;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.LOGIN;
    }

    @Override
    public void encode(ByteBuf buf) {
        Validate.notNull(tokens, "tokens cannot be empty");
        Validate.notNull(skinJwt, "skin data cannot be empty");
        buf.writeInt(protocolVersion);

        byte[] tokenBytes = tokens.getBytes(StandardCharsets.UTF_8);
        byte[] skinBytes = skinJwt.getBytes(StandardCharsets.UTF_8);

        int chunkSize = 4 + tokenBytes.length + 4 + skinBytes.length;
        VarIntUtil.writeUnsignedVarInt(buf, chunkSize);
        buf.writeIntLE(tokenBytes.length);
        buf.writeBytes(tokenBytes);
        buf.writeIntLE(skinBytes.length);
        buf.writeBytes(skinBytes);
    }

    @Override
    public void decode(ByteBuf buf) {
        protocolVersion = buf.readInt();
        ByteBuf slice = buf.readSlice((int) VarIntUtil.readUnsignedVarInt(buf));
        tokens = BedrockPacketUtil.readString(slice, slice::readIntLE);
        skinJwt = BedrockPacketUtil.readString(slice, slice::readIntLE);
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
