package org.nukkit.raknetty.channel.bedrock;

import org.nukkit.raknetty.channel.RakChannelConfig;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponder;

public interface BedrockChannelConfig extends RakChannelConfig {

    boolean isOnlineAuthenticationEnabled();

    BedrockChannelConfig setOnlineAuthenticationEnabled(boolean isOnline);

    String getUserName();

    BedrockChannelConfig setUserName(String username);

    @Override
    BedrockChannelConfig setLocalGuid(long guid);

    @Override
    BedrockChannelConfig setMtuSizes(int[] mtuSizes);

    @Override
    BedrockChannelConfig setConnectAttempts(int connectAttempts);

    @Override
    BedrockChannelConfig setConnectIntervalMillis(int connectIntervalMillis);

    @Override
    BedrockChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis);

    @Override
    BedrockChannelConfig setTimeoutMillis(int timeoutMillis);

    @Override
    BedrockChannelConfig setUnreliableTimeoutMillis(int unreliableTimeoutMillis);

    @Override
    BedrockChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds);

    @Override
    BedrockChannelConfig setOfflinePingResponder(OfflinePingResponder responder);
}
