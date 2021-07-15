package org.nukkit.raknetty.channel;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

import static org.nukkit.raknetty.handler.codec.reliability.InternalPacket.NUMBER_OF_ORDERED_STREAMS;

public record ReliableMessageEnvelop(ReliabilityMessage message,
                                     PacketPriority priority,
                                     PacketReliability reliability,
                                     int orderingChannel) {

    public ReliableMessageEnvelop {
        Validate.notNull(message);
        if (priority == null) priority = PacketPriority.HIGH_PRIORITY;
        if (reliability == null) reliability = PacketReliability.RELIABLE;
        if (orderingChannel < 0 || orderingChannel > NUMBER_OF_ORDERED_STREAMS) orderingChannel = 0;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append(message)
                .append("reliability", reliability)
                .append("priority", priority)
                .append("orderingChannel", orderingChannel)
                .toString();
    }
}
