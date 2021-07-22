package org.nukkit.raknetty.channel;

import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;

public interface ReliabilityEnvelop<M> {

    M message();

    PacketPriority priority();

    PacketReliability reliability();

    int orderingChannel();
}
