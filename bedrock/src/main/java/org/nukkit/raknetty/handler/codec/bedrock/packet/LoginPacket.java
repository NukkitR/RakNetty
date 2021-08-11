package org.nukkit.raknetty.handler.codec.bedrock.packet;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacket;
import org.nukkit.raknetty.util.ByteUtil;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class LoginPacket implements BedrockPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginPacket.class);

    public static final ECPublicKey AUTH_SERVICE_PUBLIC_KEY;
    private static final KeyFactory FACTORY;
    private static final Gson GSON = new Gson();

    static {
        try {
            FACTORY = KeyFactory.getInstance("EC");
            AUTH_SERVICE_PUBLIC_KEY = fromBase64Der("" +
                    "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyL" +
                    "cwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5" +
                    "f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90No" +
                    "KNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public LoginPacket(boolean online) {
        this.online = online;
    }

    private final boolean online;
    public int protocolVersion;
    public JsonObject extraData;
    public JsonObject skinData;
    public ECPublicKey clientKey;
    private boolean isVerified;

    @Override
    public void encode(ByteBuf buf) {
        // TODO:
        throw new UnsupportedOperationException();
    }

    @Override
    public void decode(ByteBuf buf) {
        buf.skipBytes(1);
        protocolVersion = buf.readInt();

        ByteBuf slice = buf.readSlice((int) VarIntUtil.readUnsignedVarInt(buf));
        String jwtJson = ByteUtil.readStringIntLE(slice);

        JsonObject json = GSON.fromJson(jwtJson, JsonObject.class);
        String[] tokens = GSON.fromJson(json.getAsJsonArray("chain"), String[].class);

        isVerified = verify(tokens, online ? AUTH_SERVICE_PUBLIC_KEY : null);

        Validate.isTrue(isVerified, "cannot verify web token");
        Validate.notNull(clientKey, "client key must not be null");

        String skinStr = ByteUtil.readStringIntLE(slice);
        DecodedJWT skinJws = JWT
                .require(Algorithm.ECDSA384(clientKey, null))
                .build()
                .verify(skinStr);
        skinData = payloadToJson(skinJws);
    }

    private boolean verify(String[] tokens, PublicKey authServiceKey) {

        List<ECPublicKey> trusted = new ArrayList<>();
        ECPublicKey nextKey = null;

        try {
            for (String token : tokens) {
                // read the included key from the token
                DecodedJWT untrusted = JWT.decode(token);
                String x5u = untrusted.getHeaderClaim("x5u").asString();
                ECPublicKey includedKey = fromBase64Der(x5u);

                // try to verify with the included key
                DecodedJWT jwt = JWT
                        .require(Algorithm.ECDSA384(includedKey, null))
                        .build()
                        .verify(token);

                JsonObject payload = payloadToJson(jwt);

                ECPublicKey identityPublicKey = fromBase64Der(payload.get("identityPublicKey").getAsString());
                if (nextKey != null) {
                    Validate.isTrue(nextKey.equals(includedKey));
                }
                nextKey = identityPublicKey;

                boolean certificateAuthority = payload.has("certificateAuthority")
                        && payload.get("certificateAuthority").getAsBoolean();

                if (certificateAuthority) {
                    // public key from trusted authority
                    trusted.add(identityPublicKey);
                } else {
                    if (authServiceKey != null && !trusted.contains(AUTH_SERVICE_PUBLIC_KEY)) {
                        // the token is not signed by Mojang.
                        break;
                    }

                    // public key from the client
                    clientKey = identityPublicKey;
                    extraData = payload.get("extraData").getAsJsonObject();
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public boolean isVerified() {
        return this.isVerified;
    }

    private JsonObject payloadToJson(DecodedJWT jwt) {
        return GSON.fromJson(
                new String(Base64.getDecoder().decode(jwt.getPayload())), JsonObject.class);
    }

    private static ECPublicKey fromBase64Der(String base64) throws InvalidKeySpecException {
        return (ECPublicKey) FACTORY.generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(base64)
        ));
    }

}
