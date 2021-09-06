package org.nukkit.raknetty.channel;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.ThreadLocalRandom;
import org.nukkit.raknetty.handler.codec.offline.DefaultOfflinePingResponse;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponse;
import org.nukkit.raknetty.handler.codec.reliability.SlidingWindow;

import java.util.Map;

public class DefaultRakServerChannelConfig extends DefaultChannelConfig implements RakServerChannelConfig {

    private volatile long localGuid = ThreadLocalRandom.current().nextLong();
    private volatile int numberOfInternalIds = 10;
    private volatile int rakProtocolVersion = 5;

    private volatile int maxConnections = 20;
    private volatile int maxMtuSize = SlidingWindow.MAXIMUM_MTU_SIZE;
    private volatile OfflinePingResponse.Builder<?> responseBuilder =
            new DefaultOfflinePingResponse.Builder().withMessage("Offline Data");

    private final DatagramChannel udpChannel;

    public DefaultRakServerChannelConfig(Channel channel, DatagramChannel udpChannel) {
        super(channel, new FixedRecvByteBufAllocator(2048));
        this.udpChannel = udpChannel;
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(udpChannel.config().getOptions(),
                RakServerChannelOption.RAKNET_GUID,
                RakServerChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS,
                RakServerChannelOption.RAKNET_PROTOCOL_VERSION,

                RakServerChannelOption.RAKNET_MAX_CONNECTIONS,
                RakServerChannelOption.RAKNET_MAX_MTU_SIZE,
                RakServerChannelOption.RAKNET_OFFLINE_RESPONSE_BUILDER
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RakChannelOption.RAKNET_GUID) {
            return (T) (Long) getLocalGuid();
        } else if (option == RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS) {
            return (T) (Integer) getMaximumNumberOfInternalIds();
        } else if (option == RakChannelOption.RAKNET_PROTOCOL_VERSION) {
            return (T) (Integer) getRakNetProtocolVersion();
        }

        if (option == RakServerChannelOption.RAKNET_MAX_CONNECTIONS) {
            return (T) (Integer) getMaximumConnections();
        } else if (option == RakServerChannelOption.RAKNET_MAX_MTU_SIZE) {
            return (T) (Integer) getMaximumMtuSize();
        } else if (option == RakServerChannelOption.RAKNET_OFFLINE_RESPONSE_BUILDER) {
            return (T) getOfflinePingResponseBuilder();
        }
        return udpChannel.config().getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == RakChannelOption.RAKNET_GUID) {
            setLocalGuid((long) value);
        } else if (option == RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS) {
            setMaximumNumberOfInternalIds((int) value);
        } else if (option == RakChannelOption.RAKNET_PROTOCOL_VERSION) {
            setRakNetProtocolVersion((int) value);
        }

        if (option == RakServerChannelOption.RAKNET_MAX_CONNECTIONS) {
            setMaximumConnections((int) value);
        } else if (option == RakServerChannelOption.RAKNET_MAX_MTU_SIZE) {
            setMaximumMtuSize((int) value);
        } else if (option == RakServerChannelOption.RAKNET_OFFLINE_RESPONSE_BUILDER) {
            setOfflinePingResponseBuilder((OfflinePingResponse.Builder<?>) value);
        } else {
            return udpChannel.config().setOption(option, value);
        }
        return true;
    }

    // Shared Properties
    @Override
    public long getLocalGuid() {
        return localGuid;
    }

    @Override
    public RakServerChannelConfig setLocalGuid(long guid) {
        this.localGuid = guid;
        return this;
    }

    @Override
    public int getMaximumNumberOfInternalIds() {
        return numberOfInternalIds;
    }

    @Override
    public RakServerChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds) {
        ObjectUtil.checkPositive(numberOfInternalIds, "numberOfInternalIds");
        this.numberOfInternalIds = numberOfInternalIds;
        return this;
    }

    @Override
    public int getRakNetProtocolVersion() {
        return rakProtocolVersion;
    }

    @Override
    public RakServerChannelConfig setRakNetProtocolVersion(int protocolVersion) {
        this.rakProtocolVersion = protocolVersion;
        return this;
    }


    // Server Properties
    @Override
    public int getMaximumConnections() {
        return maxConnections;
    }

    @Override
    public RakServerChannelConfig setMaximumConnections(int maxConnections) {
        ObjectUtil.checkPositiveOrZero(maxConnections, "maxConnections");
        this.maxConnections = maxConnections;
        return this;
    }

    @Override
    public int getMaximumMtuSize() {
        return maxMtuSize;
    }

    @Override
    public RakServerChannelConfig setMaximumMtuSize(int maxMtuSize) {
        ObjectUtil.checkPositiveOrZero(maxMtuSize, "maxMtuSize");
        this.maxMtuSize = maxMtuSize;
        return this;
    }

    @Override
    public OfflinePingResponse.Builder<?> getOfflinePingResponseBuilder() {
        return responseBuilder;
    }

    @Override
    public RakServerChannelConfig setOfflinePingResponseBuilder(OfflinePingResponse.Builder<?> builder) {
        this.responseBuilder = builder;
        return this;
    }

    @Override
    public boolean isAutoRead() {
        return false;
    }

    @Override
    public ChannelConfig setAutoRead(boolean autoRead) {
        throw new UnsupportedOperationException();
    }
}
