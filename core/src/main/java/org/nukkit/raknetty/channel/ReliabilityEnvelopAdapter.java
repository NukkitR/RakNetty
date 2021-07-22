package org.nukkit.raknetty.channel;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;

import static org.nukkit.raknetty.handler.codec.reliability.InternalPacket.NUMBER_OF_ORDERED_STREAMS;

class ReliabilityEnvelopAdapter<M> implements ReliabilityEnvelop<M> {

    private final M message;
    private final PacketPriority priority;
    private final PacketReliability reliability;
    private final int orderingChannel;

    public ReliabilityEnvelopAdapter(M message, PacketPriority priority, PacketReliability reliability, int orderingChannel) {
        Validate.notNull(message);
        if (priority == null) priority = PacketPriority.HIGH_PRIORITY;
        if (reliability == null) reliability = PacketReliability.RELIABLE;
        if (orderingChannel < 0 || orderingChannel > NUMBER_OF_ORDERED_STREAMS) orderingChannel = 0;

        this.message = message;
        this.priority = priority;
        this.reliability = reliability;
        this.orderingChannel = orderingChannel;
    }

    @Override
    public M message() {
        return message;
    }

    @Override
    public PacketPriority priority() {
        return priority;
    }

    @Override
    public PacketReliability reliability() {
        return reliability;
    }

    @Override
    public int orderingChannel() {
        return orderingChannel;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .append("priority", priority)
                .append("reliability", reliability)
                .append("orderingChannel", orderingChannel)
                .toString();
    }
}