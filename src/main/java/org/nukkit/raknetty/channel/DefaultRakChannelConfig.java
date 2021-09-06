package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultChannelConfig;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.ObjectUtil;
import io.netty.util.internal.ThreadLocalRandom;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.reliability.SlidingWindow;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;


public class DefaultRakChannelConfig extends DefaultChannelConfig implements RakChannelConfig {

    private volatile long localGuid = ThreadLocalRandom.current().nextLong();
    private volatile int numberOfInternalIds = 10;
    private volatile int rakProtocolVersion = 6;

    private volatile int[] mtuSizes = {SlidingWindow.MAXIMUM_MTU_SIZE, 1200, 576};
    private volatile int connectAttempts = 6;
    private volatile int connectInterval = 1000;
    private volatile int connectTimeout = 0;
    private volatile int unreliableTimeout = 0;
    private volatile int timeout = 10000;


    private final DatagramChannel udpChannel;
    private final RakChannel rakChannel;

    public DefaultRakChannelConfig(RakChannel channel, DatagramChannel udpChannel) {
        super(channel, new FixedRecvByteBufAllocator(2048));
        this.udpChannel = udpChannel;
        this.rakChannel = channel;
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(udpChannel.config().getOptions(),
                // Shared
                RakChannelOption.RAKNET_GUID,
                RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS,
                RakChannelOption.RAKNET_PROTOCOL_VERSION,
                // Channel
                RakChannelOption.RAKNET_CONNECT_MTU_SIZES,
                RakChannelOption.RAKNET_CONNECT_ATTEMPTS,
                RakChannelOption.RAKNET_CONNECT_INTERVAL,
                RakChannelOption.RAKNET_CONNECT_TIMEOUT,
                RakChannelOption.RAKNET_UNRELIABLE_TIMEOUT,
                RakChannelOption.RAKNET_TIMEOUT
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

        if (option == RakChannelOption.RAKNET_CONNECT_MTU_SIZES) {
            return (T) getConnectMtuSizes();
        } else if (option == RakChannelOption.RAKNET_CONNECT_ATTEMPTS) {
            return (T) (Integer) getConnectAttempts();
        } else if (option == RakChannelOption.RAKNET_CONNECT_INTERVAL) {
            return (T) (Integer) getConnectIntervalMillis();
        } else if (option == RakChannelOption.RAKNET_CONNECT_TIMEOUT) {
            return (T) (Integer) getConnectTimeoutMillis();
        } else if (option == RakChannelOption.RAKNET_UNRELIABLE_TIMEOUT) {
            return (T) (Integer) getUnreliableTimeoutMillis();
        } else if (option == RakChannelOption.RAKNET_TIMEOUT) {
            return (T) (Integer) getTimeoutMillis();
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

        if (option == RakChannelOption.RAKNET_CONNECT_MTU_SIZES) {
            setConnectMtuSizes((int[]) value);
        } else if (option == RakChannelOption.RAKNET_CONNECT_ATTEMPTS) {
            setConnectAttempts((int) value);
        } else if (option == RakChannelOption.RAKNET_CONNECT_INTERVAL) {
            setConnectIntervalMillis((int) value);
        } else if (option == RakChannelOption.RAKNET_CONNECT_TIMEOUT) {
            setConnectTimeoutMillis((int) value);
        } else if (option == RakChannelOption.RAKNET_UNRELIABLE_TIMEOUT) {
            setUnreliableTimeoutMillis((int) value);
        } else if (option == RakChannelOption.RAKNET_TIMEOUT) {
            setTimeoutMillis((int) value);
        } else {
            return udpChannel.config().setOption(option, value);
        }

        return true;
    }

    // Shared Properties
    @Override
    public long getLocalGuid() {
        if (rakChannel.isClient()) {
            return localGuid;
        } else {
            return rakChannel.parent().config().getLocalGuid();
        }
    }

    @Override
    public RakChannelConfig setLocalGuid(long guid) {
        Validate.isTrue(rakChannel.isClient(), "Synchronised with server.");
        this.localGuid = guid;
        return this;
    }

    @Override
    public int getMaximumNumberOfInternalIds() {
        if (rakChannel.isClient()) {
            return numberOfInternalIds;
        } else {
            return rakChannel.parent().config().getMaximumNumberOfInternalIds();
        }
    }

    @Override
    public RakChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds) {
        Validate.isTrue(rakChannel.isClient(), "Synchronised with server.");
        ObjectUtil.checkPositive(numberOfInternalIds, "numberOfInternalIds");
        this.numberOfInternalIds = numberOfInternalIds;
        return this;
    }

    @Override
    public int getRakNetProtocolVersion() {
        if (rakChannel.isClient()) {
            return rakProtocolVersion;
        } else {
            return rakChannel.parent().config().getRakNetProtocolVersion();
        }
    }

    @Override
    public RakChannelConfig setRakNetProtocolVersion(int protocolVersion) {
        Validate.isTrue(rakChannel.isClient(), "Synchronised with server.");
        this.rakProtocolVersion = protocolVersion;
        return this;
    }


    // Client Properties
    @Override
    public int[] getConnectMtuSizes() {
        return mtuSizes;
    }

    @Override
    public RakChannelConfig setConnectMtuSizes(int[] mtuSizes) {
        this.mtuSizes = Arrays.stream(mtuSizes).boxed()
                .sorted(Comparator.reverseOrder())
                .mapToInt(i -> i)
                .toArray();
        return this;
    }

    @Override
    public int getConnectAttempts() {
        return connectAttempts;
    }

    @Override
    public RakChannelConfig setConnectAttempts(int connectAttempts) {
        ObjectUtil.checkPositive(connectAttempts, "connectAttempts");
        this.connectAttempts = connectAttempts;
        return this;
    }

    @Override
    public int getConnectIntervalMillis() {
        return connectInterval;
    }

    @Override
    public RakChannelConfig setConnectIntervalMillis(int connectIntervalMillis) {
        ObjectUtil.checkPositive(connectIntervalMillis, "connectIntervalMillis");
        this.connectInterval = connectIntervalMillis;
        return this;
    }

    @Override
    public int getConnectTimeoutMillis() {
        return connectTimeout;
    }

    @Override
    public RakChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        ObjectUtil.checkPositiveOrZero(connectTimeoutMillis, "connectTimeoutMillis");
        this.connectTimeout = connectTimeoutMillis;
        return this;
    }

    @Override
    public int getTimeoutMillis() {
        return timeout;
    }

    @Override
    public RakChannelConfig setTimeoutMillis(int timeoutMillis) {
        ObjectUtil.checkPositiveOrZero(timeoutMillis, "timeoutMillis");
        this.timeout = timeoutMillis;
        return this;
    }

    @Override
    public int getUnreliableTimeoutMillis() {
        return unreliableTimeout;
    }

    @Override
    public RakChannelConfig setUnreliableTimeoutMillis(int unreliableTimeoutMillis) {
        ObjectUtil.checkPositiveOrZero(unreliableTimeoutMillis, "unreliableTimeoutMillis");
        this.unreliableTimeout = unreliableTimeoutMillis;
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
