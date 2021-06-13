package org.nukkit.raknetty.channel;

import org.nukkit.raknetty.handler.codec.Message;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;

public class BufferedReliabilityMessage<M extends Message> {

    private final PacketPriority priority;
    private final PacketReliability reliability;
    private final int orderingChannel;
    private final M message;

    public BufferedReliabilityMessage(M message, PacketPriority priority, PacketReliability reliability, int orderingChannel) {
        this.message = message;
        this.priority = priority;
        this.reliability = reliability;
        this.orderingChannel = orderingChannel;
    }
}
