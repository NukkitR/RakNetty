package org.nukkit.raknetty.channel;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;

public record ReliableMessage(ReliabilityMessage message,
                              PacketPriority priority,
                              PacketReliability reliability,
                              int orderingChannel) {

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("message", message)
                .append("reliability", reliability)
                .append("priority", priority)
                .append("orderingChannel", orderingChannel)
                .toString();
    }
}
