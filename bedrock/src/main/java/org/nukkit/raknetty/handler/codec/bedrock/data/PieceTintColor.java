package org.nukkit.raknetty.handler.codec.bedrock.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder(alphabetic = true)
public class PieceTintColor implements ByteBufSerializable {

    @JsonProperty("PieceType")
    public String pieceType;
    @JsonProperty("Colors")
    public List<String> colors;

    @Override
    public void encode(ByteBuf buf) throws Exception {
        BedrockPacketUtil.writeString(buf, pieceType);
        buf.writeIntLE(colors.size());
        colors.forEach(color -> BedrockPacketUtil.writeString(buf, color));
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        pieceType = BedrockPacketUtil.readString(buf);
        int len = (int) buf.readUnsignedIntLE();
        colors = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            colors.add(BedrockPacketUtil.readString(buf));
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
