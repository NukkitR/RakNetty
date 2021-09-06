package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;

public interface RakChannelConfig extends SharedChannelConfig {

    int[] getConnectMtuSizes();

    RakChannelConfig setConnectMtuSizes(int[] mtuSizes);

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

}
