package org.nukkit.raknetty.channel;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.ObjectUtil;
import org.nukkit.raknetty.handler.codec.DefaultOfflinePingResponder;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;


public class DefaultRakChannelConfig extends DefaultChannelConfig implements RakChannelConfig {

    private final static Random random = new Random(System.nanoTime());

    private volatile long localGuid = random.nextLong();
    private volatile int[] mtuSizes = {1492, 1200, 576}; // PPPoE,
    private volatile int connectAttempts = 6;
    private volatile int connectInterval = 1000;
    private volatile int connectTimeout = 0;
    private volatile int unreliableTimeout = 0;
    private volatile int timeout = 10000;
    private volatile int numberOfInternalIds = 10;
    private volatile OfflinePingResponder responder = new DefaultOfflinePingResponder();

    private final DatagramChannel udpChannel;

    public DefaultRakChannelConfig(Channel channel, DatagramChannel udpChannel) {
        super(channel, new FixedRecvByteBufAllocator(2048));
        this.udpChannel = udpChannel;
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(udpChannel.config().getOptions(), RakChannelOption.RAKNET_GUID);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == RakChannelOption.RAKNET_GUID) {
            return (T) (Long) getLocalGuid();
        } else if (option == RakServerChannelOption.RAKNET_MTU_SIZES) {
            return (T) getMtuSizes();
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
        } else if (option == RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS) {
            return (T) (Integer) getMaximumNumberOfInternalIds();
        } else if (option == RakChannelOption.RAKNET_OFFLINE_PING_RESPONDER) {
            return (T) getOfflinePingResponder();
        }
        return udpChannel.config().getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == RakChannelOption.RAKNET_GUID) {
            setLocalGuid((long) value);
        } else if (option == RakServerChannelOption.RAKNET_MTU_SIZES) {
            setMtuSizes((int[]) value);
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
        } else if (option == RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS) {
            setMaximumNumberOfInternalIds((int) value);
        } else if (option == RakChannelOption.RAKNET_OFFLINE_PING_RESPONDER) {
            setOfflinePingResponder((OfflinePingResponder) value);
        } else {
            return udpChannel.config().setOption(option, value);
        }

        return true;
    }

    @Override
    public long getLocalGuid() {
        return localGuid;
    }

    @Override
    public RakChannelConfig setLocalGuid(long guid) {
        this.localGuid = guid;
        return this;
    }

    @Override
    public int[] getMtuSizes() {
        return mtuSizes;
    }

    @Override
    public RakChannelConfig setMtuSizes(int[] mtuSizes) {
        this.mtuSizes = Arrays.stream(mtuSizes).boxed()
                .sorted(Comparator.reverseOrder())
                .mapToInt(i -> i)
                .toArray();
        return this;
    }

    @Override
    public int getMaximumMtuSize() {
        return mtuSizes[0]; // mtuSizes is sorted, the maximum value will be the first
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
    public int getMaximumNumberOfInternalIds() {
        return numberOfInternalIds;
    }

    @Override
    public RakChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds) {
        ObjectUtil.checkPositive(numberOfInternalIds, "numberOfInternalIds");
        this.numberOfInternalIds = numberOfInternalIds;
        return this;
    }

    @Override
    public OfflinePingResponder getOfflinePingResponder() {
        return responder;
    }

    @Override
    public RakChannelConfig setOfflinePingResponder(OfflinePingResponder responder) {
        this.responder = responder;
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
