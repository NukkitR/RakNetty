package org.nukkit.raknetty.channel.nio;

import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.DefaultRakChannelConfig;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakChannelConfig;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.Message;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.offline.ConnectionAttemptFailed;
import org.nukkit.raknetty.handler.codec.offline.ConnectionLost;
import org.nukkit.raknetty.handler.codec.offline.DisconnectionNotification;
import org.nukkit.raknetty.handler.codec.reliability.ConnectedPing;
import org.nukkit.raknetty.handler.codec.reliability.ReliabilityHandler;
import org.nukkit.raknetty.handler.codec.reliability.SlidingWindow;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class NioRakChannel extends AbstractNioRakChannel implements RakChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NioRakChannel.class);
    private static final ChannelMetadata METADATA = new ChannelMetadata(true);

    private final RakChannelConfig config;
    private SlidingWindow slidingWindow;
    private ConnectMode connectMode = ConnectMode.NO_ACTION;
    private long timeRegistered;

    private InetSocketAddress remoteAddress;
    private long remoteGuid;
    private int mtuSize;
    private Future<?> updateTask;

    private final long nextPingTime = 0;

    private final ReliabilityHandler reliabilityHandler = new ReliabilityHandler(this);


    public NioRakChannel() {
        this(null);
    }

    public NioRakChannel(final RakServerChannel parent) {
        this(parent, parent == null ? new NioDatagramChannel() : parent.udpChannel());
    }

    protected NioRakChannel(final RakServerChannel parent, final DatagramChannel udpChannel) {
        super(parent, udpChannel);
        config = new DefaultRakChannelConfig(this, udpChannel);

        pipeline().addLast("ReliabilityLayer", reliabilityHandler);
    }

    @Override
    protected void doRegister() throws Exception {
        super.doRegister();

        // we create sliding window here because only util now do we know the mtu size.
        slidingWindow = new SlidingWindow(mtuSize - SlidingWindow.UDP_HEADER_SIZE);

        // start update loop
        updateTask = eventLoop().submit(new UpdateCycleTask());

        // update time
        timeRegistered = System.nanoTime();
    }

    private void updateImmediately() {
        if (!isRegistered() || updateTask == null) {
            // channel has not yet registered.
            return;
        }

        // cancel the current loop and start a new one
        updateTask.cancel(false);
        updateTask = eventLoop().submit(new UpdateCycleTask());
    }

    private void pingInternal(PacketReliability reliability) {
        ConnectedPing ping = new ConnectedPing();
        ping.pingTime = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());

        sendImmediate();
    }

    @Override
    public ConnectMode connectMode() {
        return connectMode;
    }

    @Override
    public RakChannel connectMode(ConnectMode mode) {
        this.connectMode = mode;
        return this;
    }

    @Override
    public int mtuSize() {
        return mtuSize;
    }

    public NioRakChannel mtuSize(int mtuSize) {
        this.mtuSize = mtuSize;
        return this;
    }

    @Override
    public long localGuid() {
        return parent() == null ? config().getLocalGuid() : parent().localGuid();
    }

    @Override
    public long remoteGuid() {
        return remoteGuid;
    }

    public NioRakChannel remoteGuid(long remoteGuid) {
        this.remoteGuid = remoteGuid;
        return this;
    }

    @Override
    public SlidingWindow slidingWindow() {
        return slidingWindow;
    }

    @Override
    public RakServerChannel parent() {
        return (RakServerChannel) super.parent();
    }

    @Override
    public RakChannelConfig config() {
        return config;
    }

    @Override
    protected void doDisconnect() throws Exception {
        //TODO:
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isActive() {
        if (parent() != null) return parent().isActive();
        //TODO: check if connected
        throw new UnsupportedOperationException();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    public NioRakChannel remoteAddress(InetSocketAddress address) {
        this.remoteAddress = address;
        return this;
    }

    @Override
    public ChannelMetadata metadata() {
        return METADATA;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new NioRakUnsafe();
    }

    private final class NioRakUnsafe extends AbstractUnsafe {
        @Override
        public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            if (parent() != null && parent().isActive()) {
                NioRakChannel.this.remoteAddress = (InetSocketAddress) remoteAddress;
                return;
            }
            safeSetFailure(promise, new UnsupportedOperationException());
        }
    }

    private final class UpdateCycleTask implements Runnable {

        @Override
        public void run() {

            long currentTime = System.nanoTime();

            if (currentTime - reliabilityHandler.lastReliableSend() > config().getTimeout() / 2
                    && connectMode() == ConnectMode.CONNECTED) {
                pingInternal(PacketReliability.RELIABLE);
            }

            boolean closeConnection = false;

            // check ACK timeout
            if (reliabilityHandler.isAckTimeout(currentTime)) {
                // connection is dead
                closeConnection = true;

            } else {
                // check failure condition
                switch (connectMode) {
                    case DISCONNECT_ASAP:
                    case DISCONNECT_ASAP_SILENTLY:
                        if (!reliabilityHandler.isOutboundDataWaiting()) {
                            closeConnection = true;
                        }
                        break;

                    case DISCONNECT_ON_NO_ACK:
                        if (!reliabilityHandler.isAckWaiting()) {
                            closeConnection = true;
                        }
                        break;

                    case REQUESTED_CONNECTION:
                    case HANDLING_CONNECTION_REQUEST:
                    case UNVERIFIED_SENDER:
                        if (currentTime - timeRegistered > TimeUnit.SECONDS.toNanos(10)) {
                            closeConnection = true;
                        }
                        break;

                    default:
                        break;
                }
            }

            if (closeConnection) {

                Message closeMessage = null;
                switch (connectMode) {
                    case REQUESTED_CONNECTION:
                        closeMessage = new ConnectionAttemptFailed();
                        break;
                    case CONNECTED:
                        closeMessage = new ConnectionLost();
                        break;
                    case DISCONNECT_ASAP:
                    case DISCONNECT_ON_NO_ACK:
                        closeMessage = new DisconnectionNotification();
                        break;
                    default:
                        break;
                }

                if (closeMessage == null) {
                    //TODO: fire channel read? pipeline().fireChannelRead(closeMessage);
                }

                close();
            }

            // handle the resend queue
            reliabilityHandler.update();

            // ping if it is time to do so
            //TODO: occasional ping and lowest ping?
            if (connectMode == ConnectMode.CONNECTED
                    && currentTime - nextPingTime > 0) {

                nextPingTime = currentTime + 5000;
                pingInternal(PacketReliability.UNRELIABLE);

                // update immediately after this tick so the ping goes out right away
                //updateImmediately();
                pipeline().flush();
                return;
            }

            // schedule for next update
            updateTask = eventLoop().schedule(this, 10, TimeUnit.MILLISECONDS);
        }
    }

}
