package org.nukkit.raknetty.handler.codec.bedrock.packet;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.bedrock.AbstractBedrockPacket;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockPacketUtil;
import org.nukkit.raknetty.handler.codec.bedrock.PacketIdentifier;
import org.nukkit.raknetty.handler.codec.bedrock.data.PackIdVersion;
import org.nukkit.raknetty.handler.codec.bedrock.data.ResourcePackClientStatus;

import java.util.ArrayList;
import java.util.Collection;

public class ResourcePackClientResponsePacket extends AbstractBedrockPacket implements ClientBedrockPacket {


    public ResourcePackClientStatus status = ResourcePackClientStatus.REFUSED;
    public Collection<PackIdVersion> packIdVersions;

    @Override
    public PacketIdentifier getId() {
        return PacketIdentifier.RESOURCE_PACK_CLIENT_RESPONSE;
    }

    @Override
    public void encode(ByteBuf buf) throws Exception {
        buf.writeByte(status.ordinal());
        buf.writeShortLE(packIdVersions.size());
        for (PackIdVersion packIdVersion : packIdVersions) {
            BedrockPacketUtil.writeString(buf, packIdVersion.toString());
        }
    }

    @Override
    public void decode(ByteBuf buf) throws Exception {
        status = ResourcePackClientStatus.valueOf(buf.readUnsignedByte());
        int length = buf.readUnsignedShortLE();
        packIdVersions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            String string = BedrockPacketUtil.readString(buf);
            packIdVersions.add(new PackIdVersion(string));
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("response", status)
                .append("packIdVersions", packIdVersions)
                .toString();
    }
}
