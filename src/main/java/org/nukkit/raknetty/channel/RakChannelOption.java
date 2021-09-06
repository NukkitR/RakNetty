package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;

public class RakChannelOption<T> extends ChannelOption<T> {

    // Shared options
    public static final ChannelOption<Long> RAKNET_GUID = ChannelOption.valueOf("RAKNET_GUID");
    public static final ChannelOption<Integer> RAKNET_NUMBER_OF_INTERNAL_IDS = ChannelOption.valueOf("RAKNET_NUMBER_OF_INTERNAL_IDS");
    public static final ChannelOption<Integer> RAKNET_PROTOCOL_VERSION = ChannelOption.valueOf("RAKNET_PROTOCOL_VERSION");

    // Channel options
    public static final ChannelOption<int[]> RAKNET_CONNECT_MTU_SIZES = ChannelOption.valueOf("RAKNET_CONNECT_MTU_SIZES");
    public static final ChannelOption<Integer> RAKNET_CONNECT_INTERVAL = ChannelOption.valueOf("RAKNET_CONNECT_INTERVAL");
    public static final ChannelOption<Integer> RAKNET_CONNECT_ATTEMPTS = ChannelOption.valueOf("RAKNET_CONNECT_ATTEMPTS");
    public static final ChannelOption<Integer> RAKNET_CONNECT_TIMEOUT = ChannelOption.valueOf("RAKNET_CONNECT_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_UNRELIABLE_TIMEOUT = ChannelOption.valueOf("RAKNET_UNRELIABLE_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_TIMEOUT = ChannelOption.valueOf("RAKNET_TIMEOUT");


    @SuppressWarnings("deprecation")
    protected RakChannelOption() {
        super(null);
    }
}
