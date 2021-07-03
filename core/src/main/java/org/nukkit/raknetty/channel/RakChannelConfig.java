package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;

public interface RakChannelConfig extends ChannelConfig {

    long getLocalGuid();

    RakChannelConfig setLocalGuid(long guid);

    int getTimeout();

    RakChannelConfig setTimeout(int milliseconds);

    int getUnreliableTimeout();

    RakChannelConfig setUnreliableTimeout(int milliseconds);

    OfflinePingResponder getOfflinePingResponder();

    RakChannelConfig setOfflinePingResponder(OfflinePingResponder responder);

}
