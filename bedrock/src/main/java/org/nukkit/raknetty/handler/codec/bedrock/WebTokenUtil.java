package org.nukkit.raknetty.handler.codec.bedrock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.impl.DefaultJws;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.impl.DefaultJwtParserBuilder;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.jackson.io.JacksonSerializer;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WebTokenUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(WebTokenUtil.class);

    public static final ECPublicKey AUTH_SERVICE_PUBLIC_KEY;
    public static final ThreadLocal<KeyFactory> LOCAL_FACTORY_EC;
    public static final ObjectMapper MAPPER;
    private static final IncludedKeyResolver RESOLVER;
    private static final JwsHandler JWS_HANDLER;

    static {
        LOCAL_FACTORY_EC = ThreadLocal.withInitial(() -> {
            try {
                return KeyFactory.getInstance("EC");
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("Failed to create key factory");
            }
        });

        try {
            AUTH_SERVICE_PUBLIC_KEY = WebTokenUtil.readECPublicKey("" +
                    "MHYwEAYHKoZIzj0CAQYFK4EEACIDYgAE8ELkixyL" +
                    "cwlZryUQcu1TvPOmI2B7vX83ndnWRUaXm74wFfa5" +
                    "f/lwQNTfrLVHa2PmenpGI6JhIMUJaWZrjmMj90No" +
                    "KNFSNBuKdm8rYiXsfaz3K36x/1U26HpG0ZxK/V1V");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        MAPPER = new ObjectMapper();
        RESOLVER = new IncludedKeyResolver();
        JWS_HANDLER = new JwsHandler();
    }

    public static ECPublicKey readECPublicKey(String base64) throws InvalidKeySpecException {
        return (ECPublicKey) LOCAL_FACTORY_EC.get().generatePublic(new X509EncodedKeySpec(
                Base64.getDecoder().decode(base64)
        ));
    }

    public static String toBase64String(Key key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static Jws<Claims> verifyOnline(String tokens) throws Exception {
        return verifyTokens(tokens, AUTH_SERVICE_PUBLIC_KEY);
    }

    public static Jws<Claims> verifySelfSigned(String tokens) throws Exception {
        return verifyTokens(tokens, null);
    }

    private static Jws<Claims> verifyTokens(String tokens, PublicKey authServiceKey) throws Exception {
        JsonNode json = MAPPER.readTree(tokens);
        JsonNode chain = json.get("chain");

        Validate.isTrue(chain.isArray(), "token chain is not an array");

        List<PublicKey> trusted = new ArrayList<>();
        ECPublicKey nextKey = null;

        for (JsonNode node : chain) {
            // verify with the included key from the token
            String token = node.asText();
            Jws<Claims> jws = parse(token);

            Claims body = jws.getBody();

            ECPublicKey includedKey = readECPublicKey((String) jws.getHeader().get("x5u"));
            ECPublicKey identityPublicKey = readECPublicKey(body.get("identityPublicKey", String.class));
            if (nextKey != null) {
                Validate.isTrue(nextKey.equals(includedKey));
            }
            nextKey = identityPublicKey;

            Boolean certificateAuthority = body.get("certificateAuthority", Boolean.class);

            if (certificateAuthority != null && certificateAuthority) {
                // public key from trusted authority
                trusted.add(identityPublicKey);
            } else {
                if (authServiceKey != null && !trusted.contains(authServiceKey)) {
                    // the token is not signed by Mojang.
                    break;
                }

                return jws;
            }
        }

        LOGGER.debug("Bad chain: {}", chain);
        throw new IllegalStateException("Unable to verify the chain tokens");
    }

    public static String createSelfSigned(Map<String, Object> extraData, ECPublicKey publicKey, ECPrivateKey privateKey) {
        long iat = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()); // issue at NOW
        long nbf = iat - 60; // 60 seconds before
        long exp = iat + 24 * 3600; // 24 days after

        String b64PublicKey = toBase64String(publicKey);
        ObjectMapper mapper = MAPPER;

        try {
            JwtBuilder builder = new DefaultJwtBuilder()
                    .setHeaderParam("x5u", b64PublicKey)
                    .claim(Claims.NOT_BEFORE, nbf)
                    .claim(Claims.EXPIRATION, exp)
                    .claim("extraData", extraData)
                    .claim("identityPublicKey", b64PublicKey)
                    .serializeToJsonWith(new JacksonSerializer<>(MAPPER))
                    .signWith(privateKey, SignatureAlgorithm.ES384);

            String jwt = builder.compact();

            ObjectNode tokens = mapper.createObjectNode();
            ArrayNode chain = mapper.createArrayNode();
            chain.add(jwt);
            tokens.set("chain", chain);
            return mapper.writeValueAsString(tokens);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to write json", e);
        }
    }

    public static Jws<Claims> parse(String token) {
        return new DefaultJwtParserBuilder()
                .deserializeJsonWith(new JacksonDeserializer<>(MAPPER))
                .setSigningKeyResolver(RESOLVER)
                .build()
                .parse(token, JWS_HANDLER);
    }

    /**
     * Create a custom resolver to verify the JWS with included key
     */
    private static class IncludedKeyResolver extends SigningKeyResolverAdapter {
        @Override
        public Key resolveSigningKey(JwsHeader header, Claims claims) {
            try {
                Validate.isTrue(header.containsKey("x5u"), "Included key is missing.");
                String x5u = (String) header.get("x5u");
                return readECPublicKey(x5u);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException("Cannot read included key from header claim x5u.", e);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Key resolveSigningKey(JwsHeader header, String plaintext) {
            try {
                Map<String, ?> map = MAPPER.readValue(plaintext, new TypeReference<Map<String, ?>>() {
                });
                Claims claims = new DefaultClaims(map);
                return resolveSigningKey(header, claims);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Bad json: " + plaintext, e);
            }
        }
    }


    /**
     * Sometimes, the JWS payload will be recognised as plain text, create a custom
     * handler to fix this problem.
     */
    private static class JwsHandler extends JwtHandlerAdapter<Jws<Claims>> {
        @Override
        public Jws<Claims> onClaimsJws(Jws<Claims> jws) {
            return jws;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Jws<Claims> onPlaintextJws(Jws<String> jws) {
            String body = jws.getBody();
            try {
                Map<String, ?> map = MAPPER.readValue(body, Map.class);
                Claims claims = new DefaultClaims(map);
                return new DefaultJws<>(jws.getHeader(), claims, jws.getSignature());
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Bad json: " + body, e);
            }
        }
    }

}
