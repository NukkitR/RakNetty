package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponse;

public class RakServerChannelOption<T> extends ChannelOption<T> {

    // Shared options
    public static final ChannelOption<Long> RAKNET_GUID
            = RakChannelOption.RAKNET_GUID;
    public static final ChannelOption<Integer> RAKNET_NUMBER_OF_INTERNAL_IDS
            = RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS;
    public static final ChannelOption<Integer> RAKNET_PROTOCOL_VERSION
            = RakChannelOption.RAKNET_PROTOCOL_VERSION;

    // Reactor options
    public static final ChannelOption<Integer> RAKNET_MAX_CONNECTIONS
            = ChannelOption.valueOf("RAKNET_MAX_CONNECTION");
    public static final ChannelOption<Integer> RAKNET_MAX_MTU_SIZE
            = ChannelOption.valueOf("RAKNET_MAX_MTU_SIZE");
    public static final ChannelOption<OfflinePingResponse.Builder<?>> RAKNET_OFFLINE_RESPONSE_BUILDER
            = ChannelOption.valueOf("RAKNET_OFFLINE_RESPONSE_BUILDER");

    @SuppressWarnings("deprecation")
    protected RakServerChannelOption(String name) {
        super(null);
    }
}
