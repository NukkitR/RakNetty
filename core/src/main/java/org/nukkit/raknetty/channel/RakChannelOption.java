package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;

public class RakChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Long> RAKNET_GUID = ChannelOption.valueOf("RAKNET_GUID");
    public static final ChannelOption<int[]> RAKNET_MTU_SIZES = ChannelOption.valueOf("RAKNET_MTU_SIZES");
    public static final ChannelOption<Integer> RAKNET_CONNECT_INTERVAL = ChannelOption.valueOf("RAKNET_CONNECT_INTERVAL");
    public static final ChannelOption<Integer> RAKNET_CONNECT_ATTEMPTS = ChannelOption.valueOf("RAKNET_CONNECT_ATTEMPTS");
    public static final ChannelOption<Integer> RAKNET_CONNECT_TIMEOUT = ChannelOption.valueOf("RAKNET_CONNECT_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_UNRELIABLE_TIMEOUT = ChannelOption.valueOf("RAKNET_UNRELIABLE_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_TIMEOUT = ChannelOption.valueOf("RAKNET_TIMEOUT");
    public static final ChannelOption<Integer> RAKNET_NUMBER_OF_INTERNAL_IDS = ChannelOption.valueOf("RAKNET_NUMBER_OF_INTERNAL_IDS");
    public static final ChannelOption<OfflinePingResponder> RAKNET_OFFLINE_PING_RESPONDER = ChannelOption.valueOf("RAKNET_OFFLINE_PING_RESPONDER");

    @SuppressWarnings("deprecation")
    protected RakChannelOption() {
        super(null);
    }
}
