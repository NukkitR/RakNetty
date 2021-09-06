package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;

public interface SharedChannelConfig extends ChannelConfig {

    long getLocalGuid();

    SharedChannelConfig setLocalGuid(long guid);

    int getMaximumNumberOfInternalIds();

    SharedChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds);

    int getRakNetProtocolVersion();

    SharedChannelConfig setRakNetProtocolVersion(int protocolVersion);

}
