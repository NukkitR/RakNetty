package org.nukkit.raknetty.handler.codec.offline;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.AddressedOfflineMessage;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultServerOfflineHandler extends AbstractOfflineHandler {

    private final static InternalLogger LOGGER = InternalLoggerFactory.getInstance(DefaultServerOfflineHandler.class);
    private final static long TIMEOUT_REQUEST = TimeUnit.MILLISECONDS.toNanos(1000);
    private final static long TIMEOUT_RECENT = TimeUnit.MILLISECONDS.toNanos(100);
    public static final String NAME = "ServerOffline";

    private final Map<InetAddress, Long> recentlyConnected = new HashMap<>();
    private final Map<InetSocketAddress, Long> requested = new HashMap<>();

    private ScheduledFuture<?> updateTask;

    public DefaultServerOfflineHandler(RakServerChannel serverChannel) {
        super(serverChannel);
    }

    @Override
    public RakServerChannel channel() {
        return (RakServerChannel) super.channel();
    }

    @Override
    public void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage msg, InetSocketAddress sender) {
        OfflineMessage reply = null;
        long now = System.nanoTime();

        try {
            if (msg instanceof UnconnectedPing) {
                UnconnectedPing in = (UnconnectedPing) msg;

                if (msg instanceof UnconnectedPingOpenConnections && !channel().allowNewConnections()) {
                    return;
                }

                UnconnectedPong out = new UnconnectedPong();
                out.sendPingTime = in.sendPingTime;
                out.senderGuid = channel().localGuid();
                out.response = channel().config().getOfflinePingResponse().get(channel());
                reply = out;
                return;
            }

            //LOGGER.debug("READ: {}", msg);

            if (msg instanceof OpenConnectionRequest1) {
                OpenConnectionRequest1 in = (OpenConnectionRequest1) msg;

                // check if we are receiving a connection request from an incompatible client
                // if true, close the connection request.
                int remoteProtocol = in.protocol;
                int localProtocol = channel().config().getRakNetProtocolVersion();
                if (remoteProtocol != localProtocol) {
                    LOGGER.debug("Rejecting connection request from {}: outdated client", sender);

                    IncompatibleProtocolVersion out = new IncompatibleProtocolVersion();
                    out.protocol = localProtocol;
                    out.senderGuid = channel().localGuid();

                    reply = out;
                    return;
                }

                // save the address into the pending list,
                // and monitor if the connection attempt is timed out.
                requested.put(sender, now);

                // reply to the client with OpenConnectionReply1
                OpenConnectionReply1 out = new OpenConnectionReply1();
                out.serverGuid = channel().localGuid();
                out.mtuSize = Math.min(in.mtuSize, channel().config().getMaximumMtuSize());

                reply = out;
                return;
            }

            if (msg instanceof OpenConnectionRequest2) {
                OpenConnectionRequest2 in = (OpenConnectionRequest2) msg;

                // if the client skipped OpenConnectionRequest1 or the request was timed out
                if (!isRequestValid(sender)) {
                    LOGGER.debug("Rejecting connection request from {}: expecting OpenConnectionRequest1", sender);

                    reply = new ConnectionAttemptFailed();
                    return;
                } else {
                    // otherwise, we remove the request from pending list
                    // as it will be either rejected or accepted.
                    requested.remove(sender);
                }

                // if a connection was established from the same ip in the past 100ms
                if (isRecentlyConnected(sender)) {
                    LOGGER.debug("Rejecting connection request from {}: requested too frequently", sender);
                    IpRecentlyConnected out = new IpRecentlyConnected();
                    out.senderGuid = channel().localGuid();

                    reply = out;
                    return;
                }

                // if the server is full
                if (!channel().allowNewConnections()) {
                    LOGGER.debug("Rejecting connection request from {}: server is full", sender);
                    NoFreeIncomingConnection out = new NoFreeIncomingConnection();
                    out.senderGuid = channel().localGuid();

                    reply = out;
                    return;
                }

                // if the address or guid has been taken by someone else
                RakChannel conn = channel().getChildChannel(sender);
                // TODO: maybe we should check guid too, but i decided not to do that for now
                //boolean guidInUse = serverChannel.isGuidInUse(in.clientGuid);
                if (conn != null && conn.isActive()) {
                    LOGGER.debug("Rejecting connection request from {}: already connected", sender);
                    AlreadyConnected out = new AlreadyConnected();
                    out.senderGuid = channel().localGuid();
                    reply = out;
                    return;
                }

                // all good, allow connection
                LOGGER.debug("Accepting connection request from {}", sender);
                OpenConnectionReply2 out = new OpenConnectionReply2();
                out.serverGuid = channel().localGuid();
                out.clientAddress = sender;
                out.mtuSize = Math.min(in.mtuSize, channel().config().getMaximumMtuSize());
                reply = out;

                // creating new channel and add the address to the recently connected list
                channel().accept(ctx, in, sender);
                recentlyConnected.put(sender.getAddress(), now);
            }

        } finally {
            if (reply != null) {
                ctx.writeAndFlush(new AddressedOfflineMessage(reply, sender));
            }
        }
    }

    private boolean isRequestValid(InetSocketAddress remoteAddress) {
        Long requestedTime = requested.get(remoteAddress);

        if (requestedTime != null) {
            long currentTime = System.nanoTime();

            if (currentTime - requestedTime >= TIMEOUT_REQUEST) {
                LOGGER.debug("{} >= {}", currentTime - requestedTime, TIMEOUT_REQUEST);
                requested.remove(remoteAddress);
            } else {
                return true;
            }
        }

        return false;
    }

    private boolean isRecentlyConnected(InetSocketAddress remoteAddress) {
        InetAddress address = remoteAddress.getAddress();
        Long connectedTime = recentlyConnected.get(address);

        if (connectedTime != null) {
            long currentTime = System.nanoTime();

            if (currentTime - connectedTime >= TIMEOUT_RECENT) {
                recentlyConnected.remove(address);
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        if (updateTask == null) {
            // run the task every 60 seconds to remove all time-out requests
            // and trim recently connected list
            updateTask = ctx.executor().scheduleAtFixedRate(
                    () -> {
                        long currentTime = System.nanoTime();
                        recentlyConnected.values().removeIf(time -> currentTime - time >= TIMEOUT_RECENT);
                        requested.values().removeIf(time -> currentTime - time >= TIMEOUT_REQUEST);
                    }
                    , 0, 60, TimeUnit.SECONDS);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);

        if (updateTask != null) {
            updateTask.cancel(false);
            updateTask = null;
        }
    }
}
