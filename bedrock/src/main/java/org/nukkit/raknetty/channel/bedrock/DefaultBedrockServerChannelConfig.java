package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannel;
import org.nukkit.raknetty.channel.DefaultRakServerChannelConfig;
import org.nukkit.raknetty.channel.RakServerChannelConfig;

import java.util.Map;

public class DefaultBedrockServerChannelConfig extends DefaultBedrockChannelConfig implements BedrockServerChannelConfig, RakServerChannelConfig {

    public DefaultBedrockServerChannelConfig(Channel channel, DatagramChannel udpChannel) {
        super(channel, udpChannel);
    }

    @Override
    protected RakServerChannelConfig newRakConfig(Channel channel, DatagramChannel udpChannel) {
        return new DefaultRakServerChannelConfig(channel, udpChannel);
    }

    @Override
    protected RakServerChannelConfig rakConfig() {
        return (RakServerChannelConfig) super.rakConfig();
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);
        super.setOption(option, value);
        return true;
    }

    @Override
    public int getMaximumConnections() {
        return rakConfig().getMaximumConnections();
    }

    @Override
    public BedrockServerChannelConfig setMaximumConnections(int maxConnections) {
        rakConfig().setMaximumConnections(maxConnections);
        return this;
    }
}

