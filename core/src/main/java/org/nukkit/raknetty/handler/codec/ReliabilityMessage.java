package org.nukkit.raknetty.handler.codec;

import java.net.InetSocketAddress;

public interface ReliabilityMessage extends Message {

    public static final int MAXIMUM_NUMBER_OF_INTERNAL_IDS = 10;
    public static final InetSocketAddress UNASSIGNED_SYSTEM_ADDRESS = new InetSocketAddress("255.255.255.255", 0xFFFF);
}
