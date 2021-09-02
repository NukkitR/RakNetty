package org.nukkit.raknetty.handler.codec.bedrock.packet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;

import java.util.ArrayList;
import java.util.Collection;

public class ResourcePackClientResponsePacket implements ClientBedrockPacket {

    public Status status = Status.REFUSED;
    public Collection<String> packIdVersions;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACK_CLIENT_RESPONSE;
    }

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeEnum(status);
        buf.writeShortLE(packIdVersions.size());
        for (String packIdVersion : packIdVersions) {
            buf.writeString(packIdVersion);
        }
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        status = buf.readEnum(Status.class);
        int length = buf.readUnsignedShortLE();
        packIdVersions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            packIdVersions.add(buf.readString());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("response", status)
                .append("packIdVersions", packIdVersions)
                .toString();
    }

    public enum Status {
        NONE,
        REFUSED,
        DOWNLOADING,
        DOWNLOAD_FINISHED,
        READY;
    }
}
