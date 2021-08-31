package org.nukkit.raknetty.channel.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.*;
import org.nukkit.raknetty.handler.codec.InternalPacket;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.ReliabilityMessage;
import org.nukkit.raknetty.handler.codec.offline.DefaultClientOfflineHandler;
import org.nukkit.raknetty.handler.codec.offline.OpenConnectionRequest1;
import org.nukkit.raknetty.handler.codec.reliability.*;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ConnectionPendingException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.nukkit.raknetty.handler.codec.InternalPacket.NUMBER_OF_ORDERED_STREAMS;
import static org.nukkit.raknetty.handler.codec.Message.RAKNET_PROTOCOL_VERSION;
import static org.nukkit.raknetty.handler.codec.reliability.ReliabilityMessageHandler.MAX_PING;

public class NioRakChannel extends AbstractRakDatagramChannel implements RakChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(NioRakChannel.class);
    private static final ChannelMetadata METADATA = new ChannelMetadata(true);

    private boolean isOpen = true;

    private final RakChannelConfig config;
    private SlidingWindow slidingWindow;
    private ConnectMode connectMode = ConnectMode.NO_ACTION;
    private long timeConnected;

    private ChannelPromise connectPromise;
    private Future<?> connectTask;
    private ScheduledFuture<?> connectTimeoutFuture;

    private InetSocketAddress remoteAddress;
    private long remoteGuid;
    private int mtuSize;
    private Future<?> updateTask;

    private long nextPingTime = 0;

    private final ReliabilityInboundHandler reliabilityIn = new ReliabilityInboundHandler(this);
    private final ReliabilityOutboundHandler reliabilityOut = new ReliabilityOutboundHandler(this);
    private final ReliabilityMessageHandler messageHandler = new ReliabilityMessageHandler(this);

    public NioRakChannel() {
        this(null);
    }

    NioRakChannel(final RakServerChannel parent) {
        this(parent, parent == null ? new NioDatagramChannel() : parent.udpChannel());
    }

    NioRakChannel(final RakServerChannel parent, final DatagramChannel udpChannel) {
        super(parent, udpChannel);
        config = newConfig();

        if (isClient()) {
            DefaultClientOfflineHandler offlineHandler = new DefaultClientOfflineHandler(this);
            pipeline().addLast(DefaultClientOfflineHandler.NAME, offlineHandler);
        }

        pipeline().addLast(ReliabilityInboundHandler.NAME, reliabilityIn);
        pipeline().addLast(ReliabilityOutboundHandler.NAME, reliabilityOut);
        pipeline().addLast(ReliabilityMessageHandler.NAME, messageHandler);
    }

    protected RakChannelConfig newConfig() {
        return new DefaultRakChannelConfig(this, udpChannel());
    }

    @Override
    public final boolean isClient() {
        return parent() == null;
    }

    @Override
    public int averagePing() {
        if (!isActive()) return -1;
        return messageHandler.averagePing();
    }

    @Override
    public boolean isActive() {
        return isOpen && connectMode.isConnected();
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    protected void doClose() throws Exception {
        isOpen = false;

        if (isClient()) {
            udpChannel().close();
        } else {
            parent().removeChildChannel(remoteAddress());
        }

        if (connectPromise != null) {
            connectPromise.tryFailure(new ConnectTimeoutException());
            connectPromise = null;
        }

        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (!isClient()) throw new UnsupportedOperationException();

        if (localAddress == null) {
            localAddress = new InetSocketAddress(0);
        }
        doBind(localAddress);

        // already connected
        if (isActive()) return true;

        connectTask = eventLoop().submit(new ConnectionRequestTask());
        return false;
    }

    @Override
    protected void doFinishConnect() {
        if (connectPromise != null) {
            connectPromise.trySuccess();
        }
    }

    protected void doFinishConnect(Throwable cause) {
        if (connectPromise != null) {
            connectPromise.tryFailure(cause);
        }
    }

    @Override
    public ChannelFuture disconnect() {
        return disconnect(newPromise());
    }

    @Override
    public ChannelFuture disconnect(ChannelPromise promise) {
        if (isOpen() && connectMode.canDisconnect()) {

            // send notification
            DisconnectionNotification out = new DisconnectionNotification();
            send(out, PacketPriority.LOW_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);
            updateImmediately();

            // wait until the disconnect notification is acknowledged
            connectMode(ConnectMode.DISCONNECT_ASAP);
            return super.disconnect(promise);
        }
        return promise;
    }

    @Override
    protected void doDisconnect() throws Exception {
        // NOOP
    }

    @Override
    protected void doRegister() throws Exception {
        super.doRegister();

        // start update loop
        updateTask = eventLoop().submit(new UpdateCycleTask());
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

    public void ping(PacketReliability reliability) {
        //LOGGER.debug("PING");
        ConnectedPing ping = new ConnectedPing();
        ping.pingTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        send(ping, PacketPriority.IMMEDIATE_PRIORITY, reliability, 0);
    }

    @Override
    public void send(final ReliabilityMessage message, final PacketPriority priority, final PacketReliability reliability, final int orderingChannel) {
        if (!eventLoop().inEventLoop()) {
            eventLoop().execute(() -> NioRakChannel.this.send(message, priority, reliability, orderingChannel));

        } else {
            // create with a slightly larger capacity to optimise for most split packets
            ByteBuf buf = alloc().ioBuffer(2048);
            try {
                message.getId().writeTo(buf);
                message.encode(buf);
                send(buf, priority, reliability, orderingChannel);

            } catch (Exception e) {
                ReferenceCountUtil.release(buf);
                pipeline().fireExceptionCaught(e);
            }
        }
    }

    @Override
    public void send(final ByteBuf message, final PacketPriority priority, final PacketReliability reliability, final int orderingChannel) {
        if (!eventLoop().inEventLoop()) {
            eventLoop().execute(() -> NioRakChannel.this.send(message, priority, reliability, orderingChannel));

        } else {
            InternalPacket packet = new InternalPacket();
            packet.data = message;

            packet.reliability = (reliability == null ? PacketReliability.RELIABLE : reliability);
            packet.priority = (priority == null ? PacketPriority.HIGH_PRIORITY : priority);
            packet.orderingChannel = (orderingChannel < 0 || orderingChannel > NUMBER_OF_ORDERED_STREAMS ? 0 : orderingChannel);

            pipeline().context(messageHandler).write(packet);
            //pipeline().write(packet);

            if (priority == PacketPriority.IMMEDIATE_PRIORITY) {
                updateImmediately();
            }
        }
    }

    @Override
    public ConnectMode connectMode() {
        return connectMode;
    }

    @Override
    public NioRakChannel connectMode(ConnectMode mode) {
        boolean wasActive = isActive();
        connectMode = mode;

        if (mode == ConnectMode.UNVERIFIED_SENDER || mode == ConnectMode.REQUESTED_CONNECTION) {
            // update time
            if (timeConnected <= 0) timeConnected = System.nanoTime();
        }

        if (!wasActive && mode == ConnectMode.CONNECTED) {
            pipeline().fireChannelActive();
            doFinishConnect();
        }
        return this;
    }

    @Override
    public int mtuSize() {
        return mtuSize;
    }

    @Override
    public NioRakChannel mtuSize(int mtuSize) {
        Validate.isTrue(slidingWindow == null, "mtu size is immutable and cannot be changed.");
        Validate.isTrue(mtuSize > 0, "mtu size must be positive");

        mtuSize = Math.min(mtuSize, config().getMaximumMtuSize());
        this.mtuSize = mtuSize;

        slidingWindow = new SlidingWindow(this, mtuSize);
        LOGGER.debug("Sliding window is created (mtu = {}).", mtuSize);

        return this;
    }

    @Override
    public long localGuid() {
        return isClient() ? config().getLocalGuid() : parent().localGuid();
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
    public InetSocketAddress remoteAddress() {
        return remoteAddress;
    }

    protected NioRakChannel remoteAddress(InetSocketAddress address) {
        Validate.isTrue(remoteAddress == null, "remote address has already been assigned");
        remoteAddress = address;
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
            if (!isClient()) {
                throw new UnsupportedOperationException();
            }

            if (!promise.setUncancellable() || !ensureOpen(promise)) {
                return;
            }

            try {
                if (connectPromise != null) {
                    // Already a connect in process.
                    throw new ConnectionPendingException();
                }

                if (!(remoteAddress instanceof InetSocketAddress)) {
                    promise.tryFailure(new ConnectException("remote address is not a subclass of InetSocketAddress"));
                    return;
                }

                boolean wasActive = isActive();
                if (doConnect(remoteAddress, localAddress)) {
                    fulfillConnectPromise(promise, wasActive);

                } else {
                    connectPromise = promise;
                    NioRakChannel.this.remoteAddress = (InetSocketAddress) remoteAddress;

                    // Schedule connect timeout.
                    int connectTimeoutMillis = config().getConnectTimeoutMillis();
                    if (connectTimeoutMillis > 0) {
                        connectTimeoutFuture = eventLoop().schedule(() -> {
                            ChannelPromise connectPromise = NioRakChannel.this.connectPromise;

                            if (connectPromise != null && !connectPromise.isDone()
                                    && connectPromise.tryFailure(new ConnectTimeoutException(
                                    "connection timed out: " + remoteAddress))) {
                                close(voidPromise());
                            }
                        }, connectTimeoutMillis, TimeUnit.MILLISECONDS);
                    }

                    promise.addListener((ChannelFutureListener) future -> {
                        if (future.isDone()) {
                            if (connectTimeoutFuture != null) {
                                connectTimeoutFuture.cancel(false);
                            }

                            if (connectTask != null) {
                                connectTask.cancel(false);
                            }

                            connectPromise = null;

                            if (future.isSuccess()) {
                                LOGGER.debug("CONNECT: SUCCESS");
                            } else {
                                // connection is not successful
                                if (isOpen()) {
                                    close(voidPromise());
                                }

                                if (future.isCancelled()) {
                                    LOGGER.debug("CONNECT: CANCELLED");
                                } else {
                                    LOGGER.debug("CONNECT: FAILED");
                                }
                            }
                        }
                    });
                }
            } catch (Throwable t) {
                promise.tryFailure(annotateConnectException(t, remoteAddress));
                closeIfClosed();
            }
        }

        private void fulfillConnectPromise(ChannelPromise promise, boolean wasActive) {
            if (promise == null) {
                return;
            }

            boolean promiseSet = promise.trySuccess();

            if (!promiseSet) {
                close(voidPromise());
            }
        }
    }

    private final class ConnectionRequestTask implements Runnable {

        private int requestsMade = 0;

        @Override
        public void run() {

            int attempts = config().getConnectAttempts();
            int[] mtuSizes = config().getMtuSizes();
            int mtuNum = mtuSizes.length;

            if (connectMode() == ConnectMode.REQUESTED_CONNECTION || connectMode() == ConnectMode.CONNECTED) {
                return;
            }

            if (requestsMade >= attempts) {
                connectPromise.tryFailure(new ConnectTimeoutException());
                return;
            }

            int mtuIndex = requestsMade / (attempts / mtuNum);
            if (mtuIndex >= mtuNum) mtuIndex = mtuNum - 1;

            requestsMade++;

            // schedule next connection request
            connectTask = eventLoop().schedule(this, config().getConnectIntervalMillis(), TimeUnit.MILLISECONDS);

            if (remoteAddress() != null) {
                OpenConnectionRequest1 request = new OpenConnectionRequest1();
                request.protocol = RAKNET_PROTOCOL_VERSION;
                request.mtuSize = mtuSizes[mtuIndex];

                udpChannel().writeAndFlush(new AddressedOfflineMessage(request, remoteAddress()));
            }
        }
    }

    private final class UpdateCycleTask implements Runnable {
        @Override
        public void run() {

            boolean closeConnection = false;

            try {
                long currentTime = System.nanoTime();

                if (currentTime - reliabilityOut.lastReliableSend() > TimeUnit.MILLISECONDS.toNanos(config().getTimeoutMillis() / 2)
                        && connectMode() == ConnectMode.CONNECTED) {
                    // send a ping when the resend list is empty so that disconnection is noticed.
                    if (!reliabilityOut.isOutboundDataWaiting()) {
                        ping(PacketReliability.RELIABLE);
                    }
                }

                // check ACK timeout
                if (reliabilityOut.isAckTimeout(currentTime)) {
                    // connection is dead
                    LOGGER.debug("ACK timed out.");
                    closeConnection = true;

                } else {
                    // check failure condition
                    switch (connectMode) {
                        case DISCONNECT_ASAP:
                        case DISCONNECT_ASAP_SILENTLY:
                            if (!reliabilityOut.isOutboundDataWaiting()) {
                                closeConnection = true;
                            }
                            break;

                        case DISCONNECT_ON_NO_ACK:
                            if (!reliabilityOut.isAckWaiting()) {
                                closeConnection = true;
                            }
                            break;

                        case REQUESTED_CONNECTION:
                        case HANDLING_CONNECTION_REQUEST:
                        case UNVERIFIED_SENDER:
                            if (currentTime - timeConnected > TimeUnit.SECONDS.toNanos(10)) {
                                closeConnection = true;
                            }
                            break;

                        default:
                            break;
                    }
                }

                if (closeConnection) {
                    // TODO: replace with disconnect event
                    /*
                    ByteBuf closeMessage = alloc().ioBuffer(1, 1);
                    switch (connectMode) {
                        case REQUESTED_CONNECTION:
                            ByteUtil.writeByte(closeMessage, MessageIdentifier.ID_CONNECTION_ATTEMPT_FAILED);
                            break;
                        case CONNECTED:
                            ByteUtil.writeByte(closeMessage, MessageIdentifier.ID_CONNECTION_LOST);
                            break;
                        case DISCONNECT_ASAP:
                        case DISCONNECT_ON_NO_ACK:
                            ByteUtil.writeByte(closeMessage, MessageIdentifier.ID_DISCONNECTION_NOTIFICATION);
                            break;
                        default:
                            ReferenceCountUtil.release(closeMessage);
                            break;
                    }

                    if (closeMessage.isReadable()) {
                        pipeline().context(messageHandler).fireChannelRead(closeMessage);
                    }*/

                    if (isOpen()) {
                        close();
                    }

                    return;
                }

                // handle the resend queue
                reliabilityOut.update();

                // ping if it is time to do so
                if (connectMode == ConnectMode.CONNECTED
                        && currentTime - nextPingTime > 0
                        && messageHandler.lowestPing() == MAX_PING) {

                    nextPingTime = currentTime + TimeUnit.SECONDS.toNanos(5); // ping every 5 seconds
                    ping(PacketReliability.UNRELIABLE);
                }

            } catch (Exception e) {
                LOGGER.debug(e);

            } finally {
                // schedule for next update
                if (!closeConnection) {
                    updateTask = eventLoop().schedule(this, 10, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}
