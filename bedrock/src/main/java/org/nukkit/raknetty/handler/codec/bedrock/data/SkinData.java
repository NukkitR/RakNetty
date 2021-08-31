package org.nukkit.raknetty.handler.codec.bedrock.data;

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
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.WebTokenUtil;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class SkinData implements ByteBufSerializable {

    public static final byte[] SKIN_DATA_WHITE_64_64;

    static {
        byte[] color = {0, 0, 0, (byte) 255};
        SKIN_DATA_WHITE_64_64 = new byte[64 * 64 * 4];
        for (int i = 0; i < SKIN_DATA_WHITE_64_64.length; i += 4) {
            System.arraycopy(color, 0, SKIN_DATA_WHITE_64_64, i, 4);
        }
    }

    @JsonProperty("AnimatedImageData")
    public List<AnimatedImageData> animatedImageData = new ArrayList<>();
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
    public boolean isCapeOnClassicSkin = false;
    @JsonProperty("ClientRandomId")
    public long clientRandomId = -1;
    @JsonProperty("CurrentInputMode")
    public int currentInputMode = 1;
    @JsonProperty("DefaultInputMode")
    public int defaultInputMode = 1;
    @JsonProperty("DeviceId")
    public String deviceId = "";
    @JsonProperty("DeviceModel")
    public String deviceModel = "";
    @JsonProperty("DeviceOS")
    public int deviceOS = 7;
    @JsonProperty("GameVersion")
    public String gameVersion = "";
    @JsonProperty("GuiScale")
    public int guiScale = 0;
    @JsonProperty("LanguageCode")
    public String languageCode = "";
    @JsonProperty("PersonaPieces")
    public List<PersonaPiece> personaPieces = new ArrayList<>();
    @JsonProperty("PersonaSkin")
    public boolean isPersonaSkin = false;
    @JsonProperty("PieceTintColors")
    public List<PieceTintColor> pieceTintColors = new ArrayList<>();
    @JsonProperty("PlatformOfflineId")
    public String platformOfflineId = "";
    @JsonProperty("PlatformOnlineId")
    public String platformOnlineId = "";
    @JsonProperty("PlayFabId")
    public String playFabId = "";
    @JsonProperty("PremiumSkin")
    public boolean isPremiumSkin = false;
    @JsonProperty("SelfSignedId")
    public String selfSignedId = "";
    @JsonProperty("ServerAddress")
    public String serverAddress = "";
    @JsonProperty("SkinAnimationData")
    public String skinAnimationData = "";
    @JsonProperty("SkinColor")
    public String skinColor = "";
    @JsonProperty("SkinData")
    public String skinData = "";
    @JsonProperty("SkinGeometryData")
    public String skinGeometryData = "";
    @JsonProperty("SkinId")
    public String skinId = "";
    @JsonProperty("SkinImageHeight")
    public int skinImageHeight = 0;
    @JsonProperty("SkinImageWidth")
    public int skinImageWidth = 0;
    @JsonProperty("SkinResourcePatch")
    public String skinResourcePatch = "";
    @JsonProperty("ThirdPartyName")
    public String thirdPartyName = "";
    @JsonProperty("ThirdPartyNameOnly")
    public boolean thirdPartyNameOnly = false;
    @JsonProperty("UIProfile")
    public int uIProfile = 0;

    @Override
    public void encode(ByteBuf buf) throws Exception {
        // string skinId
        BedrockPacketUtil.writeString(buf, skinId);
        // string playFabId
        BedrockPacketUtil.writeString(buf, playFabId);
        // string SkinResourcePatch base64_decode
        BedrockPacketUtil.writeString(buf, new String(Base64.getDecoder().decode(skinResourcePatch), StandardCharsets.UTF_8));
        // uint SkinImageWidth
        buf.writeIntLE(skinImageWidth);
        // uint SkinImageHeight
        buf.writeIntLE(skinImageHeight);
        // string SkinData
        BedrockPacketUtil.writeBytes(buf, Base64.getDecoder().decode(skinData));
        // array[] AnimatedImageData
        BedrockPacketUtil.writeList(buf, animatedImageData, buf::writeIntLE);
        // uint CapeImageWidth
        buf.writeIntLE(capeImageWidth);
        // uint CapeImageHeight
        buf.writeIntLE(capeImageHeight);
        // string capeData
        BedrockPacketUtil.writeBytes(buf, Base64.getDecoder().decode(capeData));
        // string SkinGeometry
        BedrockPacketUtil.writeString(buf, new String(Base64.getDecoder().decode(skinGeometryData), StandardCharsets.UTF_8));
        // string SkinAnimationData
        BedrockPacketUtil.writeString(buf, new String(Base64.getDecoder().decode(skinAnimationData), StandardCharsets.UTF_8));
        // bool isPremium
        buf.writeBoolean(isPremiumSkin);
        // bool isPersonaSkin
        buf.writeBoolean(isPersonaSkin);
        // bool isCapeOnClassicSkin
        buf.writeBoolean(isCapeOnClassicSkin);
        // string CapeId
        BedrockPacketUtil.writeString(buf, capeId);
        // string SkinId
        BedrockPacketUtil.writeString(buf, skinId);
        // string ArmSize
        BedrockPacketUtil.writeString(buf, armSize);
        // string SkinColor
        BedrockPacketUtil.writeString(buf, skinColor);
        // array[] PersonaPiece
        BedrockPacketUtil.writeList(buf, personaPieces, buf::writeIntLE);
        // array[] PieceTintColors
        BedrockPacketUtil.writeList(buf, pieceTintColors, buf::writeIntLE);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        skinId = BedrockPacketUtil.readString(buf);
        playFabId = BedrockPacketUtil.readString(buf);
        skinResourcePatch = Base64.getEncoder().encodeToString(BedrockPacketUtil.readString(buf).getBytes(StandardCharsets.UTF_8));
        skinImageWidth = buf.readIntLE();
        skinImageHeight = buf.readIntLE();
        skinData = Base64.getEncoder().encodeToString(BedrockPacketUtil.readByteArray(buf));
        animatedImageData = BedrockPacketUtil.readList(buf, AnimatedImageData.class, buf::readIntLE);
        capeImageWidth = buf.readIntLE();
        capeImageHeight = buf.readIntLE();
        capeData = Base64.getEncoder().encodeToString(BedrockPacketUtil.readByteArray(buf));
        skinGeometryData = Base64.getEncoder().encodeToString(BedrockPacketUtil.readString(buf).getBytes(StandardCharsets.UTF_8));
        skinAnimationData = Base64.getEncoder().encodeToString(BedrockPacketUtil.readString(buf).getBytes(StandardCharsets.UTF_8));
        isPremiumSkin = buf.readBoolean();
        isPersonaSkin = buf.readBoolean();
        isCapeOnClassicSkin = buf.readBoolean();
        capeId = BedrockPacketUtil.readString(buf);
        BedrockPacketUtil.readString(buf);
        armSize = BedrockPacketUtil.readString(buf);
        skinColor = BedrockPacketUtil.readString(buf);
        personaPieces = BedrockPacketUtil.readList(buf, PersonaPiece.class, buf::readIntLE);
        pieceTintColors = BedrockPacketUtil.readList(buf, PieceTintColor.class, buf::readIntLE);
    }

    public static SkinData fromWebToken(String skinJwt) {
        ObjectMapper mapper = WebTokenUtil.MAPPER;

        Jws<Claims> jws = WebTokenUtil.parse(skinJwt);
        Map<String, ?> map = jws.getBody();
        return mapper.convertValue(map, SkinData.class);
    }

    public static SkinData generate() {
        SkinData skin = new SkinData();
        skin.clientRandomId = ThreadLocalRandom.current().nextLong();
        skin.deviceId = UUID.randomUUID().toString();
        skin.languageCode = "zh_CN";
        skin.playFabId = "1234567890abcdef";
        skin.selfSignedId = UUID.randomUUID().toString();
        skin.skinColor = "#0";
        skin.skinData = Base64.getEncoder().encodeToString(SKIN_DATA_WHITE_64_64);
        skin.skinId = "c18e65aa-7b21-4637-9b63-8ad63622ef01.CustomSlim" + UUID.randomUUID();
        skin.skinImageWidth = 64;
        skin.skinImageHeight = 64;
        skin.skinResourcePatch = "ewogICAiZ2VvbWV0cnkiIDogewogICAgICAiZGVmYXVsdCIgOiAiZ2VvbWV0cnkuaHVtYW5vaWQuY3VzdG9tU2xpbSIKICAgfQp9Cg==";
        return skin;
    }

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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("animatedImageData", animatedImageData)
                .append("armSize", armSize)
                .append("capeData", capeData)
                .append("capeId", capeId)
                .append("capeImageHeight", capeImageHeight)
                .append("capeImageWidth", capeImageWidth)
                .append("isCapeOnClassicSkin", isCapeOnClassicSkin)
                .append("clientRandomId", clientRandomId)
                .append("currentInputMode", currentInputMode)
                .append("defaultInputMode", defaultInputMode)
                .append("deviceId", deviceId)
                .append("deviceModel", deviceModel)
                .append("deviceOS", deviceOS)
                .append("gameVersion", gameVersion)
                .append("guiScale", guiScale)
                .append("languageCode", languageCode)
                .append("personaPieces", personaPieces)
                .append("isPersonaSkin", isPersonaSkin)
                .append("pieceTintColors", pieceTintColors)
                .append("platformOfflineId", platformOfflineId)
                .append("platformOnlineId", platformOnlineId)
                .append("playFabId", playFabId)
                .append("isPremiumSkin", isPremiumSkin)
                .append("selfSignedId", selfSignedId)
                .append("serverAddress", serverAddress)
                .append("skinAnimationData", skinAnimationData)
                .append("skinColor", skinColor)
                //.append("skinData", skinData)
                .append("skinGeometryData", skinGeometryData)
                .append("skinId", skinId)
                .append("skinImageHeight", skinImageHeight)
                .append("skinImageWidth", skinImageWidth)
                .append("skinResourcePatch", skinResourcePatch)
                .append("thirdPartyName", thirdPartyName)
                .append("thirdPartyNameOnly", thirdPartyNameOnly)
                .append("uIProfile", uIProfile)
                .toString();
    }
}
