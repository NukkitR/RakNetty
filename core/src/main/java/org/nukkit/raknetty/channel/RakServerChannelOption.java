package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;

public class RakServerChannelOption<T> extends RakChannelOption<T> {

    public static final ChannelOption<Integer> RAKNET_MAX_CONNECTIONS = valueOf("RAKNET_MAX_CONNECTION");
}
