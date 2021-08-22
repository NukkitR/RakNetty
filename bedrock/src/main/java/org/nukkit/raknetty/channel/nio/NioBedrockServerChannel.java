package org.nukkit.raknetty.channel.nio;

import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.BedrockServerChannel;
import org.nukkit.raknetty.channel.bedrock.BedrockServerChannelConfig;
import org.nukkit.raknetty.channel.bedrock.DefaultBedrockServerChannelConfig;

import java.net.InetSocketAddress;

public class NioBedrockServerChannel extends NioRakServerChannel implements BedrockServerChannel {

    public NioBedrockServerChannel() {
        super();
        config().setMaximumNumberOfInternalIds(20);
        config().setMaximumConnections(20);
        config().setMtuSizes(new int[]{1400});
    }

    @Override
    protected BedrockChannel newChildChannel(InetSocketAddress remoteAddress, long guid) {
        return new NioBedrockChannel(this)
                .remoteAddress(remoteAddress)
                .remoteGuid(guid);
    }

    @Override
    protected BedrockServerChannelConfig newConfig() {
        return new DefaultBedrockServerChannelConfig(this, udpChannel());
    }

    @Override
    public BedrockServerChannelConfig config() {
        return (BedrockServerChannelConfig) super.config();
    }
}
