package org.nukkit.raknetty.handler.codec.bedrock.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;

public class PersonaPiece implements ByteBufSerializable {

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
    public void encode(ByteBuf buf) throws Exception {
        BedrockPacketUtil.writeString(buf, pieceId);
        BedrockPacketUtil.writeString(buf, pieceType);
        BedrockPacketUtil.writeString(buf, packId);
        buf.writeBoolean(isDefault);
        BedrockPacketUtil.writeString(buf, productId);
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        pieceId = BedrockPacketUtil.readString(buf);
        pieceType = BedrockPacketUtil.readString(buf);
        packId = BedrockPacketUtil.readString(buf);
        isDefault = buf.readBoolean();
        productId = BedrockPacketUtil.readString(buf);
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
