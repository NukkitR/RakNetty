package org.nukkit.raknetty.channel;

public interface RakServerChannelConfig extends RakChannelConfig {

    int getMaximumConnections();

    RakServerChannelConfig setMaximumConnections(int maxConnections);
}
