package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.util.ReferenceCountUtil;
import org.nukkit.raknetty.handler.codec.InternalPacket;

import java.util.*;

public class SplitPacketList {

    private final Map<Integer, SplitPacketChannel> splitChannels = new HashMap<>();

    public void insert(InternalPacket packet) {
        int splitPacketId = packet.splitPacketId;
        SplitPacketChannel channel = splitChannels.get(splitPacketId);

        if (channel == null) {
            channel = new SplitPacketChannel();
            splitChannels.put(splitPacketId, channel);
        }

        channel.packets.put(packet.splitPacketIndex, packet);
        // TODO: implement Download Progress Event
    }

    public InternalPacket build(int splitPacketId) {
        SplitPacketChannel channel = splitChannels.get(splitPacketId);
        if (channel == null) return null;

        Collection<InternalPacket> packets = channel.packets.values();
        if (packets.isEmpty()) return null;

        if (packets.size() == packets.stream().findFirst().get().splitPacketCount) {
            InternalPacket packet = build(channel);
            splitChannels.remove(splitPacketId);
            return packet;
        }

        return null;
    }

    private InternalPacket build(SplitPacketChannel channel) {
        // copy every bytes to the first packet
        Iterator<InternalPacket> iterator = channel.packets.values().iterator();
        InternalPacket packet = iterator.next();

        while (iterator.hasNext()) {
            InternalPacket p = iterator.next();
            packet.data.writeBytes(p.data, p.bodyLength());

            // release all the other packets except the first one
            ReferenceCountUtil.release(p);
        }

        return packet;
    }

    private static class SplitPacketChannel {
        public final Map<Integer, InternalPacket> packets = new TreeMap<>();
    }
}
