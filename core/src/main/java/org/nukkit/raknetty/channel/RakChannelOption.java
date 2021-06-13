package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;

public class RakChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Long> RAKNET_GUID = ChannelOption.valueOf("RAKNET_GUID");
    public static final ChannelOption<Integer> RAKNET_UNRELIABLE_TIMEOUT = ChannelOption.valueOf("RAKNET_UNRELIABLE_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_TIMEOUT = ChannelOption.valueOf("RAKNET_TIMEOUT");

    @SuppressWarnings("deprecation")
    protected RakChannelOption() {
        super(null);
    }
}
