package org.nukkit.raknetty.handler.codec.bedrock.serialization;

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
import io.netty.buffer.ByteBufUtil;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.WebTokenUtil;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class SerializedSkin implements NetworkSerializable {

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
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeString(skinId);
        buf.writeString(playFabId);
        buf.writeString(new String(Base64.getDecoder().decode(skinResourcePatch), StandardCharsets.UTF_8));
        buf.writeIntLE(skinImageWidth);
        buf.writeIntLE(skinImageHeight);
        buf.writeByteArray(Base64.getDecoder().decode(skinData));
        buf.writeList(animatedImageData, buf::writeIntLE);
        buf.writeIntLE(capeImageWidth);
        buf.writeIntLE(capeImageHeight);
        buf.writeByteArray(Base64.getDecoder().decode(capeData));
        buf.writeString(new String(Base64.getDecoder().decode(skinGeometryData), StandardCharsets.UTF_8));
        buf.writeString(new String(Base64.getDecoder().decode(skinAnimationData), StandardCharsets.UTF_8));
        buf.writeBoolean(isPremiumSkin);
        buf.writeBoolean(isPersonaSkin);
        buf.writeBoolean(isCapeOnClassicSkin);
        buf.writeString(capeId);
        buf.writeString(skinId);
        buf.writeString(armSize);
        buf.writeString(skinColor);
        buf.writeList(personaPieces, buf::writeIntLE);
        buf.writeList(pieceTintColors, buf::writeIntLE);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        skinId = buf.readString();
        playFabId = buf.readString();
        skinResourcePatch = Base64.getEncoder().encodeToString(buf.readString().getBytes(StandardCharsets.UTF_8));
        skinImageWidth = buf.readIntLE();
        skinImageHeight = buf.readIntLE();
        skinData = Base64.getEncoder().encodeToString(buf.readByteArray());
        animatedImageData = buf.readList(AnimatedImageData::new, buf::readIntLE);
        capeImageWidth = buf.readIntLE();
        capeImageHeight = buf.readIntLE();
        capeData = Base64.getEncoder().encodeToString(buf.readByteArray());
        skinGeometryData = Base64.getEncoder().encodeToString(buf.readString().getBytes(StandardCharsets.UTF_8));
        skinAnimationData = Base64.getEncoder().encodeToString(buf.readString().getBytes(StandardCharsets.UTF_8));
        isPremiumSkin = buf.readBoolean();
        isPersonaSkin = buf.readBoolean();
        isCapeOnClassicSkin = buf.readBoolean();
        capeId = buf.readString();
        buf.readString();
        armSize = buf.readString();
        skinColor = buf.readString();
        personaPieces = buf.readList(PersonaPiece::new, buf::readIntLE);
        pieceTintColors = buf.readList(PieceTintColor::new, buf::readIntLE);
    }

    public static SerializedSkin fromWebToken(String skinJwt) {
        ObjectMapper mapper = WebTokenUtil.MAPPER;

        Jws<Claims> jws = WebTokenUtil.parse(skinJwt);
        Map<String, ?> map = jws.getBody();
        return mapper.convertValue(map, SerializedSkin.class);
    }

    public static SerializedSkin randomSkin() {
        SerializedSkin skin = new SerializedSkin();
        skin.clientRandomId = ThreadLocalRandom.current().nextLong();
        skin.deviceId = UUID.randomUUID().toString();
        skin.languageCode = "zh_CN";
        byte[] bytes = new byte[8];
        ThreadLocalRandom.current().nextBytes(bytes);
        skin.playFabId = ByteBufUtil.hexDump(bytes);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class AnimatedImageData implements NetworkSerializable {
        @JsonProperty("Image")
        public String image = "";
        @JsonProperty("ImageWidth")
        public int imageWidth = 64;
        @JsonProperty("ImageHeight")
        public int imageHeight = 64;
        @JsonProperty("Frames")
        public float frames = 0;
        @JsonProperty("AnimationExpression")
        public int animationExpression;
        @JsonProperty("Type")
        public int type;

        @Override
        public void encode(BedrockByteBuf buf) throws Exception {
            buf.writeIntLE(imageWidth);
            buf.writeIntLE(imageHeight);
            buf.writeBytes(Base64.getDecoder().decode(image));
            buf.writeIntLE(type);
            buf.writeFloatLE(frames);
            buf.writeIntLE(animationExpression);
        }

        @Override
        public void decode(BedrockByteBuf buf) throws Exception {
            imageWidth = buf.readIntLE();
            imageHeight = buf.readIntLE();
            image = Base64.getEncoder().encodeToString(buf.readByteArray());
            type = buf.readIntLE();
            frames = buf.readFloatLE();
            animationExpression = buf.readIntLE();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("image", image)
                    .append("imageWidth", imageWidth)
                    .append("imageHeight", imageHeight)
                    .append("frames", frames)
                    .append("animationExpression", animationExpression)
                    .append("type", type)
                    .toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class PersonaPiece implements NetworkSerializable {
        @JsonProperty("PieceId")
        public String pieceId;
        @JsonProperty("PieceType")
        public String pieceType;
        @JsonProperty("PackId")
        public String packId;
        @JsonProperty("IsDefault")
        public boolean isDefault;
        @JsonProperty("ProductId")
        public String productId;

        @Override
        public void encode(BedrockByteBuf buf) throws Exception {
            buf.writeString(pieceId);
            buf.writeString(pieceType);
            buf.writeString(packId);
            buf.writeBoolean(isDefault);
            buf.writeString(productId);
        }

        @Override
        public void decode(BedrockByteBuf buf) throws Exception {
            pieceId = buf.readString();
            pieceType = buf.readString();
            packId = buf.readString();
            isDefault = buf.readBoolean();
            productId = buf.readString();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("pieceId", pieceId)
                    .append("pieceType", pieceType)
                    .append("packId", packId)
                    .append("isDefault", isDefault)
                    .append("productId", productId)
                    .toString();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonPropertyOrder(alphabetic = true)
    public static class PieceTintColor implements NetworkSerializable {
        @JsonProperty("PieceType")
        public String pieceType;
        @JsonProperty("Colors")
        public List<String> colors;

        @Override
        public void encode(BedrockByteBuf buf) throws Exception {
            buf.writeString(pieceType);
            buf.writeIntLE(colors.size());
            colors.forEach(buf::writeString);
        }

        @Override
        public void decode(BedrockByteBuf buf) throws Exception {
            pieceType = buf.readString();
            int len = (int) buf.readUnsignedIntLE();
            colors = new ArrayList<>(len);
            for (int i = 0; i < len; i++) {
                colors.add(buf.readString());
            }
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("pieceType", pieceType)
                    .append("colors", colors)
                    .toString();
        }
    }
}
