package org.nukkit.raknetty.channel;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import org.nukkit.raknetty.handler.codec.DefaultOfflinePingResponder;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;

import java.util.Map;
import java.util.Random;


public class DefaultRakChannelConfig extends DefaultChannelConfig implements RakChannelConfig {

    private final static Random random = new Random(System.nanoTime());

    private volatile long localGuid = random.nextLong();
    private volatile int unreliableTimeout = 0;
    private volatile int timeout = 10000;
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
            return (T) (Long) localGuid;
        } else if (option == RakChannelOption.RAKNET_UNRELIABLE_TIMEOUT) {
            return (T) (Integer) unreliableTimeout;
        } else if (option == RakChannelOption.RAKNET_TIMEOUT) {
            return (T) (Integer) timeout;
        } else if (option == RakChannelOption.RAKNET_OFFLINE_PING_RESPONDER) {
            return (T) responder;
        }
        return udpChannel.config().getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == RakChannelOption.RAKNET_GUID) {
            localGuid = (long) value;
        } else if (option == RakChannelOption.RAKNET_UNRELIABLE_TIMEOUT) {
            unreliableTimeout = (int) value;
        } else if (option == RakChannelOption.RAKNET_TIMEOUT) {
            timeout = (int) value;
        } else if (option == RakChannelOption.RAKNET_OFFLINE_PING_RESPONDER) {
            responder = (OfflinePingResponder) value;
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
    public int getTimeout() {
        return timeout;
    }

    @Override
    public RakChannelConfig setTimeout(int milliseconds) {
        this.timeout = milliseconds;
        return this;
    }

    @Override
    public int getUnreliableTimeout() {
        return unreliableTimeout;
    }

    @Override
    public RakChannelConfig setUnreliableTimeout(int milliseconds) {
        this.unreliableTimeout = milliseconds;
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
