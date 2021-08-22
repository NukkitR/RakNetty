package org.nukkit.raknetty.channel.bedrock;

import org.nukkit.raknetty.channel.RakServerChannelConfig;

public interface BedrockServerChannelConfig extends RakServerChannelConfig {

    @Override
    BedrockServerChannelConfig setMaximumConnections(int maxConnections);
}
