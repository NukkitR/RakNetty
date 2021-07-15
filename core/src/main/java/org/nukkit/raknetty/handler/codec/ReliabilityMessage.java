package org.nukkit.raknetty.handler.codec;

import java.net.InetSocketAddress;

public interface ReliabilityMessage extends Message {

    //int MAXIMUM_NUMBER_OF_INTERNAL_IDS = 10;
    InetSocketAddress UNASSIGNED_SYSTEM_ADDRESS = new InetSocketAddress("255.255.255.255", 0xFFFF);
}
