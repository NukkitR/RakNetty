package org.nukkit.raknetty.channel;

import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponse;

public interface RakServerChannelConfig extends SharedChannelConfig {

    int getMaximumConnections();

    RakServerChannelConfig setMaximumConnections(int maxConnections);

    int getMaximumMtuSize();

    RakServerChannelConfig setMaximumMtuSize(int maxMtuSize);

    OfflinePingResponse getOfflinePingResponse();

    RakServerChannelConfig setOfflinePingResponseBuilder(OfflinePingResponse response);
}
