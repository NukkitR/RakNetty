package org.nukkit.raknetty.channel;

public interface BedrockServerChannelConfig extends RakServerChannelConfig {

    @Override
    BedrockServerChannelConfig setMaximumConnections(int maxConnections);
}
