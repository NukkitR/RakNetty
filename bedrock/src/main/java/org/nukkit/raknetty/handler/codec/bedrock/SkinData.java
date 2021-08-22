package org.nukkit.raknetty.handler.codec.bedrock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.jackson.io.JacksonSerializer;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class SkinData {

    public static final String SKIN_DATA_WHITE_64_64;

    static {
        byte[] color = {0, 0, 0, (byte) 255};
        byte[] data = new byte[64 * 64 * 4];
        for (int i = 0; i < data.length; i += 4) {
            System.arraycopy(color, 0, data, i, 4);
        }
        SKIN_DATA_WHITE_64_64 = Base64.getEncoder().encodeToString(data);
    }

    @JsonProperty("AnimatedImageData")
    public List<Object> animatedImageData = new ArrayList<>();
    @JsonProperty("ArmSize")
    public String armSize = "";
    @JsonProperty("CapeData")
    public String capeData = "";
    @JsonProperty("CapeId")
    public String capeId = "";
    @JsonProperty("CapeImageHeight")
    public int capeImageHeight = 0;
    @JsonProperty("CapeImageWidth")
    public int capeImageWidth = 0;
    @JsonProperty("CapeOnClassicSkin")
    public boolean capeOnClassicSkin = false;
    @JsonProperty("ClientRandomId")
    public long clientRandomId = ThreadLocalRandom.current().nextLong();
    @JsonProperty("CurrentInputMode")
    public int currentInputMode = 1;
    @JsonProperty("DefaultInputMode")
    public int defaultInputMode = 1;
    @JsonProperty("DeviceId")
    public String deviceId = UUID.randomUUID().toString();
    @JsonProperty("DeviceModel")
    public String deviceModel = "";
    @JsonProperty("DeviceOS")
    public int deviceOS = 7;
    @JsonProperty("GameVersion")
    public String gameVersion = "1.17.10";
    @JsonProperty("GuiScale")
    public int guiScale = 0;
    @JsonProperty("LanguageCode")
    public String languageCode = "en_GB";
    @JsonProperty("PersonaPieces")
    public List<Object> personaPieces = new ArrayList<>();
    @JsonProperty("PersonaSkin")
    public boolean personaSkin = false;
    @JsonProperty("PieceTintColors")
    public List<Object> pieceTintColors = new ArrayList<>();
    @JsonProperty("PlatformOfflineId")
    public String platformOfflineId = "";
    @JsonProperty("PlatformOnlineId")
    public String platformOnlineId = "";
    @JsonProperty("PlayFabId")
    public String playFabId = "";
    @JsonProperty("PremiumSkin")
    public boolean premiumSkin = false;
    @JsonProperty("SelfSignedId")
    public String selfSignedId = UUID.randomUUID().toString();
    @JsonProperty("ServerAddress")
    public String serverAddress = "localhost:19132";
    @JsonProperty("SkinAnimationData")
    public String skinAnimationData = "";
    @JsonProperty("SkinColor")
    public String skinColor = "#0";
    @JsonProperty("SkinData")
    public String skinData = SKIN_DATA_WHITE_64_64;
    @JsonProperty("SkinGeometryData")
    public String skinGeometryData = Base64.getEncoder().encodeToString("null".getBytes(StandardCharsets.UTF_8));
    @JsonProperty("SkinId")
    public String skinId = UUID.randomUUID().toString() + ".CustomSlim";
    @JsonProperty("SkinImageHeight")
    public int skinImageHeight = 64;
    @JsonProperty("SkinImageWidth")
    public int skinImageWidth = 64;
    @JsonProperty("SkinResourcePatch") // base64_decode the following to see what it means
    public String skinResourcePatch = "ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tU2xpbSIKICAgfQp9Cg==";
    @JsonProperty("ThirdPartyName")
    public String thirdPartyName = "Steve";
    @JsonProperty("ThirdPartyNameOnly")
    public boolean thirdPartyNameOnly = false;
    @JsonProperty("UIProfile")
    public int uIProfile = 0;

    public static SkinData fromWebToken(String skinJwt) {
        ObjectMapper mapper = WebTokenUtil.MAPPER;

        Jws<Claims> jws = WebTokenUtil.parse(skinJwt);
        Map<String, ?> map = jws.getBody();
        return mapper.convertValue(map, SkinData.class);
    }

    @SuppressWarnings("unchecked")
    public String sign(ECPublicKey publicKey, ECPrivateKey privateKey) {
        ObjectMapper mapper = WebTokenUtil.MAPPER;
        String b64PublicKey = WebTokenUtil.toBase64String(publicKey);

        return new DefaultJwtBuilder()
                .setHeaderParam("x5u", b64PublicKey)
                .setClaims(mapper.convertValue(this, new TypeReference<Map<String, Object>>() {
                }))
                .serializeToJsonWith(new JacksonSerializer<>(mapper))
                .signWith(privateKey, SignatureAlgorithm.ES384)
                .compact();
    }
}
