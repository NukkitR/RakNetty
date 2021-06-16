package org.nukkit.raknetty.channel.nio;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.DefaultRakServerChannelConfig;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.channel.RakServerChannelConfig;
import org.nukkit.raknetty.handler.codec.offline.DefaultServerOfflineHandler;
import org.nukkit.raknetty.handler.codec.offline.OpenConnectionRequest2;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public final class NioRakServerChannel extends AbstractNioRakChannel implements RakServerChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NioRakServerChannel.class);
    private static final ChannelMetadata METADATA = new ChannelMetadata(false, 16);

    private final RakServerChannelConfig config;

    Map<InetSocketAddress, RakChannel> childChannels = new HashMap<>();

    public NioRakServerChannel() {
        this(new NioDatagramChannel());
    }

    protected NioRakServerChannel(DatagramChannel udpChannel) {
        super(null, udpChannel);
        config = new DefaultRakServerChannelConfig(this, udpChannel);

        pipeline().addLast(new DefaultServerOfflineHandler(this));
        pipeline().addLast(new ServerMessageDispatcher());
    }

    @Override
    public void accept(ChannelHandlerContext ctx, OpenConnectionRequest2 request, InetSocketAddress remoteAddress) {
        NioRakChannel channel = new NioRakChannel(this)
                .remoteAddress(remoteAddress)
                .remoteGuid(request.clientGuid)
                .mtuSize(request.mtuSize);

        childChannels.put(remoteAddress, channel);
        ctx.fireChannelRead(channel);
    }

    @Override
    public boolean allowNewConnections() {
        long connected = childChannels.values().stream()
                .filter(channel -> channel.isActive() && channel.connectMode() == RakChannel.ConnectMode.CONNECTED)
                .count();

        return connected < config().getMaximumConnections();
    }

    @Override
    public RakChannel getChannel(InetSocketAddress address) {
        // check if it is called from the thread
        if (!eventLoop().inEventLoop()) return null;
        return childChannels.get(address);
    }

    @Override
    public RakChannel getChannel(long guid) {
        // check if it is called from the thread
        if (!eventLoop().inEventLoop()) return null;

        return childChannels.values().stream()
                .filter(channel -> channel.remoteGuid() == guid)
                .findFirst()
                .orElse(null);
    }

    @Override
    public long remoteGuid() {
        return -1; //UNASSIGNED_SYSTEM_ADDRESS
    }

    @Override
    public long localGuid() {
        return config().getLocalGuid();
    }

    @Override
    protected void doDisconnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive() {
        return udpChannel().isActive();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return null;
    }


    @Override
    public RakServerChannelConfig config() {
        return config;
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new NioRakServerUnsafe();
    }

    private final class NioRakServerUnsafe extends AbstractUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            safeSetFailure(promise, new UnsupportedOperationException());
        }
    }

    private class ServerMessageDispatcher extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof DatagramPacket) {
                InetSocketAddress address = ((DatagramPacket) msg).sender();
                RakChannel channel = getChannel(address);
                if (channel != null) {
                    channel.pipeline().fireChannelRead(msg);
                }

            } else if (msg instanceof Channel) {
                // probably a new RakNettyChannel created from upstream handler,
                // proceed it further to the ServerBootstrapAcceptor
                ctx.fireChannelRead(msg);
            } else {
                ReferenceCountUtil.release(msg);
            }
        }
    }
}
