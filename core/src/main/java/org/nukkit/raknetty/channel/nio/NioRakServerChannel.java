package org.nukkit.raknetty.channel.nio;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.*;
import org.nukkit.raknetty.handler.codec.offline.DefaultServerOfflineHandler;
import org.nukkit.raknetty.handler.codec.offline.OpenConnectionRequest2;
import org.nukkit.raknetty.handler.ipfilter.BannedIpFilter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;

public class NioRakServerChannel extends AbstractRakDatagramChannel implements RakServerChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NioRakServerChannel.class);
    private static final ChannelMetadata METADATA = new ChannelMetadata(false);

    private final RakServerChannelConfig config;
    private final BannedIpFilter banList = new BannedIpFilter(this);

    Map<InetSocketAddress, RakChannel> childChannels = new HashMap<>();

    public NioRakServerChannel() {
        this(new NioDatagramChannel());
    }

    protected NioRakServerChannel(DatagramChannel udpChannel) {
        super(null, udpChannel);
        config = newConfig();

        pipeline().addLast("BanList", banList);
        pipeline().addLast(new DefaultServerOfflineHandler(this));
        pipeline().addLast(new ServerMessageDispatcher());
    }

    protected RakServerChannelConfig newConfig() {
        return new DefaultRakServerChannelConfig(this, udpChannel());
    }

    @Override
    public void accept(ChannelHandlerContext ctx, OpenConnectionRequest2 request, InetSocketAddress remoteAddress) {
        RakChannel channel = newChildChannel(remoteAddress, request.clientGuid)
                .mtuSize(request.mtuSize)
                .connectMode(RakChannel.ConnectMode.UNVERIFIED_SENDER);

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

    protected RakChannel newChildChannel(InetSocketAddress remoteAddress, long guid) {
        return new NioRakChannel(this)
                .remoteAddress(remoteAddress)
                .remoteGuid(guid);
    }

    @Override
    public RakChannel getChildChannel(InetSocketAddress address) {
        // check if it is called from the thread
        if (!eventLoop().inEventLoop()) return null;
        return childChannels.get(address);
    }

    @Override
    public void removeChildChannel(InetSocketAddress address) {
        if (!eventLoop().inEventLoop()) {
            eventLoop().submit(() -> this.removeChildChannel(address));
            return;
        }

        Channel channel = childChannels.remove(address);
        LOGGER.debug("Remove child channel: {}", channel);
    }

    public RakChannel getChildChannel(long guid) {
        // check if it is called from the thread
        if (!eventLoop().inEventLoop()) return null;

        return childChannels.values().stream()
                .filter(channel -> channel.remoteGuid() == guid)
                .findFirst()
                .orElse(null);
    }

    @Override
    public BannedIpFilter banList() {
        return banList;
    }

    @Override
    protected void doClose() throws Exception {
        udpChannel().close();
        childChannels.values().forEach(channel -> channel.close());
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doFinishConnect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long remoteGuid() {
        return -1;
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
    public boolean isOpen() {
        return udpChannel().isOpen();
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
                RakChannel channel = getChildChannel(address);
                if (channel != null) {
                    channel.eventLoop().submit(() -> {
                        channel.pipeline().fireChannelRead(msg);
                    });
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
