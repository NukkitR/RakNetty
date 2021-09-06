package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponse;

public interface RakServerChannelConfig extends SharedChannelConfig {

    int getMaximumConnections();

    RakServerChannelConfig setMaximumConnections(int maxConnections);

    int getMaximumMtuSize();

    RakServerChannelConfig setMaximumMtuSize(int maxMtuSize);

    OfflinePingResponse.Builder<?> getOfflinePingResponseBuilder();

    RakServerChannelConfig setOfflinePingResponseBuilder(OfflinePingResponse.Builder<?> builder);
}
