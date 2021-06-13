package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;

public class RakServerChannelOption<T> extends RakChannelOption<T> {

    public static final ChannelOption<Integer> RAKNET_MAX_CONNECTIONS = ChannelOption.valueOf("RAKNET_MAX_CONNECTION");
    public static final ChannelOption<int[]> RAKNET_MTU_SIZES = ChannelOption.valueOf("RAKNET_MTU_SIZES");

}
