package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;

public interface RakChannelConfig extends ChannelConfig {

    long getLocalGuid();

    RakChannelConfig setLocalGuid(long guid);

    int[] getMtuSizes();

    RakChannelConfig setMtuSizes(int[] mtuSizes);

    int getMaximumMtuSize();

    int getConnectAttempts();

    RakChannelConfig setConnectAttempts(int connectAttempts);

    int getConnectIntervalMillis();

    RakChannelConfig setConnectIntervalMillis(int connectIntervalMillis);

    @Override
    int getConnectTimeoutMillis();

    @Override
    RakChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    int getTimeoutMillis();

    RakChannelConfig setTimeoutMillis(int timeoutMillis);

    int getUnreliableTimeoutMillis();

    RakChannelConfig setUnreliableTimeoutMillis(int unreliableTimeoutMillis);

    int getMaximumNumberOfInternalIds();

    RakChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds);

    OfflinePingResponder getOfflinePingResponder();

    RakChannelConfig setOfflinePingResponder(OfflinePingResponder responder);

}
