package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.ProtocolUtil;
import org.nukkit.raknetty.handler.codec.bedrock.WebTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Base64;

public class ServerToClientHandshake extends AbstractBedrockPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerToClientHandshake.class);

    public ECPrivateKey serverPrivateKey;
    public ECPublicKey serverPublicKey;
    public byte[] salt;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.SERVER_TO_CLIENT_HANDSHAKE;
    }

    @Override
    public void encode(ByteBuf buf) {
        Validate.notNull(serverPrivateKey, "private key cannot be null");
        Validate.notNull(serverPublicKey, "public key cannot be null");
        Validate.notNull(salt, "salt cannot be empty");

        String token = new DefaultJwtBuilder()
                .setHeaderParam("x5u", WebTokenUtil.toBase64String(serverPublicKey))
                .claim("salt", Base64.getEncoder().encodeToString(salt))
                .serializeToJsonWith(new JacksonSerializer<>(WebTokenUtil.MAPPER))
                .signWith(serverPrivateKey, SignatureAlgorithm.ES384)
                .compact();

        ProtocolUtil.writeString(buf, token);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        String token = ProtocolUtil.readString(buf);
        Validate.notNull(token, "failed to read web token");

        Jws<Claims> jws = WebTokenUtil.parse(token);
        serverPublicKey = WebTokenUtil.readECPublicKey((String) jws.getHeader().get("x5u"));
        String salt = jws.getBody().get("salt", String.class);
        this.salt = Base64.getDecoder().decode(salt);
    }
}
