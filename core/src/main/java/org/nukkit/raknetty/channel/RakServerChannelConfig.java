package org.nukkit.raknetty.channel;

public interface RakServerChannelConfig extends RakChannelConfig {

    int getMaximumConnections();

    RakServerChannelConfig setMaximumConnections(int maxConnections);

    int[] getMtuSizes();

    RakServerChannelConfig setMtuSizes(int[] mtuSizes);
}
