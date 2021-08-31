package org.nukkit.raknetty.handler.codec.bedrock.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

import java.util.Base64;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class AnimatedImageData implements ByteBufSerializable {

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
    public void encode(ByteBuf buf) throws Exception {
        buf.writeIntLE(imageWidth);
        buf.writeIntLE(imageHeight);
        BedrockPacketUtil.writeBytes(buf, Base64.getDecoder().decode(image));
        buf.writeIntLE(type);
        buf.writeFloatLE(frames);
        buf.writeIntLE(animationExpression);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        imageWidth = buf.readIntLE();
        imageHeight = buf.readIntLE();
        image = Base64.getEncoder().encodeToString(BedrockPacketUtil.readByteArray(buf));
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
