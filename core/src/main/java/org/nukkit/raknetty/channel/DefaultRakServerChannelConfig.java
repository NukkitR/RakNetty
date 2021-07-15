package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;
import io.netty.channel.socket.DatagramChannel;

import java.util.Map;

public class DefaultRakServerChannelConfig extends DefaultRakChannelConfig implements RakServerChannelConfig {

    private volatile int maxConnections = 20;

    public DefaultRakServerChannelConfig(RakServerChannel channel, DatagramChannel udpChannel) {
        super(channel, udpChannel);
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(super.getOptions(), RakServerChannelOption.RAKNET_MAX_CONNECTIONS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RakServerChannelOption.RAKNET_MAX_CONNECTIONS) {
            return (T) (Integer) maxConnections;
        }
        return super.getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == RakServerChannelOption.RAKNET_MAX_CONNECTIONS) {
            maxConnections = (int) value;
        } else {
            return super.setOption(option, value);
        }
        return true;
    }

    @Override
    public int getMaximumConnections() {
        return maxConnections;
    }

    @Override
    public RakServerChannelConfig setMaximumConnections(int maxConnections) {
        this.maxConnections = maxConnections;
        return this;
    }
}
