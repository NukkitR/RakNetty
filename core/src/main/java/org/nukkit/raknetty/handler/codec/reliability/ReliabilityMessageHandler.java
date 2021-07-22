package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakChannel.ConnectMode;
import org.nukkit.raknetty.channel.ReliableMessageEnvelop;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.util.PacketUtil;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ReliabilityMessageHandler extends ChannelDuplexHandler {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReliabilityMessageHandler.class);
    private static final int PING_TIMES_ARRAY_SIZE = 5;
    public static final int MAX_PING = 0xffff;

    private final RakChannel channel;

    private int lowestPing = MAX_PING;
    private final int[] pingTime = new int[PING_TIMES_ARRAY_SIZE];
    private final long[] clockDiff = new long[PING_TIMES_ARRAY_SIZE];
    private int pingArrayIndex = 0;

    public ReliabilityMessageHandler(RakChannel channel) {
        this.channel = channel;
        Arrays.fill(pingTime, MAX_PING);
    }

    public int lowestPing() {
        return lowestPing;
    }

    public int averagePing() {
        if (!channel.isActive()) return -1;
        long sum = 0;
        int num = 0;

        for (int i = 0; i < PING_TIMES_ARRAY_SIZE; i++) {
            if (pingTime[i] == MAX_PING) break;

            sum += pingTime[i];
            num++;
        }

        if (num > 0) return (int) (sum / num) & 0xffff;

        return -1;
    }

    public long clockDifference() {
        if (!channel.isActive()) return 0;

        int min = MAX_PING;
        long diff = 0;

        for (int i = 0; i < PING_TIMES_ARRAY_SIZE; i++) {
            if (pingTime[i] == MAX_PING) break;

            if (pingTime[i] < min) {
                diff = clockDiff[i];
                min = pingTime[i];
            }
        }

        return diff;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        boolean release = true;

        MessageIdentifier id = PacketUtil.getMessageIdentifier(buf);

        ConnectMode connectMode = channel.connectMode();

        // For unknown senders we only accept a few specific packets
        if (connectMode == ConnectMode.UNVERIFIED_SENDER) {
            if (id != MessageIdentifier.ID_CONNECTION_REQUEST) {
                channel.close();

                LOGGER.debug("Temporarily banning {} for sending bad data", channel.remoteAddress());
                channel.parent().banList().add(channel.remoteAddress(), channel.config().getTimeoutMillis());

                return;
            }
        }

        // if we cannot look up for a valid id, then it might be a custom packet, better pass it to the user
        if (id == null) {
            id = MessageIdentifier.ID_USER_PACKET_ENUM;
        }

        // try to decode
        try {
            switch (id) {
                case ID_CONNECTION_REQUEST -> {
                    if (connectMode == ConnectMode.UNVERIFIED_SENDER || connectMode == ConnectMode.REQUESTED_CONNECTION) {
                        ConnectionRequest in = new ConnectionRequest();
                        in.decode(buf);
                        channel.connectMode(ConnectMode.HANDLING_CONNECTION_REQUEST);

                        LOGGER.debug("CONNECTING: {}", in);

                        ConnectionRequestAccepted out = new ConnectionRequestAccepted(channel.config().getMaximumNumberOfInternalIds());
                        out.clientAddress = channel.remoteAddress();
                        out.requestTime = in.requestTime;
                        out.replyTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                        channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);
                    }
                }
                case ID_NEW_INCOMING_CONNECTION -> {
                    if (channel.connectMode() == ConnectMode.HANDLING_CONNECTION_REQUEST) {

                        // set channel state to CONNECTED
                        channel.connectMode(ConnectMode.CONNECTED);

                        // ping the remote peer
                        channel.ping(PacketReliability.UNRELIABLE);

                        NewIncomingConnection in = new NewIncomingConnection(channel.config().getMaximumNumberOfInternalIds());
                        in.decode(buf);

                        LOGGER.debug("CONNECTED: {}", in);

                        onConnectedPong(in.pingTime, in.pongTime);
                    }
                }
                case ID_CONNECTED_PONG -> {
                    ConnectedPong in = new ConnectedPong();
                    in.decode(buf);

                    onConnectedPong(in.pingTime, in.pongTime);

                    LOGGER.debug("PONG_RECV: {} ms", averagePing());
                }
                case ID_CONNECTED_PING -> {
                    ConnectedPing in = new ConnectedPing();
                    in.decode(buf);

                    long currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                    LOGGER.debug("PING_RECV");

                    ConnectedPong out = new ConnectedPong();
                    out.pingTime = in.pingTime;
                    out.pongTime = currentTime;

                    channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.UNRELIABLE, 0);
                }
                case ID_DISCONNECTION_NOTIFICATION -> {
                    LOGGER.debug("READ: ID_DISCONNECTION_NOTIFICATION");
                    // do not close the channel immediately as we need to ack the ID_DISCONNECTION_NOTIFICATION
                    channel.connectMode(ConnectMode.DISCONNECT_ON_NO_ACK);
                }
                // case ID_DETECT_LOST_CONNECTIONS:
                // case ID_INVALID_PASSWORD:
                case ID_CONNECTION_REQUEST_ACCEPTED -> {

                    // if we are in a situation to wait to be connected
                    boolean canConnect = connectMode == ConnectMode.HANDLING_CONNECTION_REQUEST ||
                            connectMode == ConnectMode.REQUESTED_CONNECTION;

                    // if we are already connected
                    boolean hasConnected = connectMode == ConnectMode.HANDLING_CONNECTION_REQUEST;

                    if (canConnect) {

                        ConnectionRequestAccepted in = new ConnectionRequestAccepted(channel.config().getMaximumNumberOfInternalIds());
                        in.decode(buf);

                        onConnectedPong(in.requestTime, in.replyTime);

                        LOGGER.debug("CONNECTED: {}", in);

                        channel.connectMode(ConnectMode.CONNECTED);

                        NewIncomingConnection out = new NewIncomingConnection(channel.config().getMaximumNumberOfInternalIds());
                        out.serverAddress = channel.remoteAddress();
                        out.pingTime = in.replyTime;
                        out.pongTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                        channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);

                        if (!hasConnected) {
                            channel.ping(PacketReliability.UNRELIABLE);
                        }
                    }
                }
                default -> {
                    // give the rest to the user
                    release = false;
                    ctx.fireChannelRead(msg);
                }
            }
        } catch (Exception e) {
            LOGGER.debug("READ: bad packet {} from {}", id, channel.remoteAddress());
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    private void onConnectedPong(long pingTime, long pongTime) {
        long currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
        int ping = (int) Math.max(currentTime - pingTime, 0) & MAX_PING;

        this.pingTime[pingArrayIndex] = ping;
        this.clockDiff[pingArrayIndex] = pongTime - (currentTime / 2 + pingTime / 2);

        lowestPing = Math.min(ping, lowestPing) & MAX_PING;

        pingArrayIndex = (pingArrayIndex + 1) % PING_TIMES_ARRAY_SIZE;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ReliableMessageEnvelop envelop = (ReliableMessageEnvelop) msg;

        int mtuSize = channel.mtuSize();
        ByteBuf buf = ctx.alloc().ioBuffer(mtuSize, mtuSize);
        envelop.message().encode(buf);

        InternalPacket packet = new InternalPacket();
        packet.data = buf;

        packet.reliability = envelop.reliability();
        packet.priority = envelop.priority();
        packet.orderingChannel = envelop.orderingChannel();

        ctx.write(packet);
    }
}
