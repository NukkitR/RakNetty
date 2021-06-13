package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;

public interface RakChannelConfig extends ChannelConfig {

    long getLocalGuid();

    RakChannelConfig setLocalGuid(long guid);

    int getTimeout();

    RakChannelConfig setTimeout(int milliseconds);

    int getUnreliableTimeout();

    RakChannelConfig setUnreliableTimeout(int milliseconds);

}
