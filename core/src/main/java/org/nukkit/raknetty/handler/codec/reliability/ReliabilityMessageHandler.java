package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakChannel.ConnectMode;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.util.PacketUtil;

import java.util.concurrent.TimeUnit;

public class ReliabilityMessageHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReliabilityMessageHandler.class);
    private static final int PING_TIMES_ARRAY_SIZE = 5;

    private final RakChannel channel;

    private short lowestPing = Short.MAX_VALUE;
    private final short[] pingTime = new short[PING_TIMES_ARRAY_SIZE];
    private final long[] clockDiff = new long[PING_TIMES_ARRAY_SIZE];
    private int pingArrayIndex = 0;

    public ReliabilityMessageHandler(RakChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        boolean release = true;

        //LOGGER.debug("READ: \n{}", ByteBufUtil.prettyHexDump(buf));

        try {

            MessageIdentifier id = PacketUtil.getMessageIdentifier(buf);
            if (id == null) {
                // it is not an valid id
                return;
            }

            ConnectMode connectMode = channel.connectMode();

            // For unknown senders we only accept a few specific packets
            if (connectMode == ConnectMode.UNVERIFIED_SENDER) {
                if (id != MessageIdentifier.ID_CONNECTION_REQUEST) {
                    channel.close();

                    LOGGER.debug("Temporarily banning {} for sending bad data", channel.remoteAddress());
                    channel.parent().banList().add(channel.remoteAddress(), channel.config().getTimeout());

                    return;
                }
            }

            switch (id) {
                case ID_CONNECTION_REQUEST: {
                    if (connectMode == ConnectMode.UNVERIFIED_SENDER || connectMode == ConnectMode.REQUESTED_CONNECTION) {
                        ConnectionRequest in = new ConnectionRequest();
                        in.decode(buf);
                        channel.connectMode(ConnectMode.HANDLING_CONNECTION_REQUEST);

                        LOGGER.debug("CONNECTING: {}", in);

                        ConnectionRequestAccepted out = new ConnectionRequestAccepted();
                        out.clientAddress = channel.remoteAddress();
                        out.requestTime = in.requestTime;
                        out.replyTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                        channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);
                    }
                    break;
                }
                case ID_NEW_INCOMING_CONNECTION: {
                    if (channel.connectMode() == ConnectMode.HANDLING_CONNECTION_REQUEST) {

                        // set channel state to CONNECTED
                        channel.connectMode(ConnectMode.CONNECTED);
                        ctx.pipeline().fireChannelActive();

                        channel.ping(PacketReliability.UNRELIABLE);

                        NewIncomingConnection in = new NewIncomingConnection();
                        in.decode(buf);

                        LOGGER.debug("CONNECTED: {}", in);

                        onConnectedPong(in.pingTime, in.pongTime);
                    }
                    break;
                }
                case ID_CONNECTED_PONG: {

                    ConnectedPong in = new ConnectedPong();
                    in.decode(buf);

                    LOGGER.debug("PONG_RECV: {}", in);

                    onConnectedPong(in.pingTime, in.pongTime);

                    break;
                }
                case ID_CONNECTED_PING: {

                    ConnectedPing in = new ConnectedPing();
                    in.decode(buf);

                    LOGGER.debug("PING_RECV: {}", in);

                    ConnectedPong out = new ConnectedPong();
                    out.pingTime = in.pingTime;
                    out.pongTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                    channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.UNRELIABLE, 0);
                    break;
                }
                case ID_DISCONNECTION_NOTIFICATION: {
                    // do not close the channel immediately as we need to ack the ID_DISCONNECTION_NOTIFICATION
                    channel.connectMode(ConnectMode.DISCONNECT_ON_NO_ACK);
                    break;
                }
                // case ID_DETECT_LOST_CONNECTIONS:
                // case ID_INVALID_PASSWORD:
                case ID_CONNECTION_REQUEST_ACCEPTED: {
                    boolean allowConnection, alreadyConnected;

                    // TODO: parse and reply
                    break;
                }
                default:
                    // give the rest to the user
                    release = false;
                    ctx.fireChannelRead(msg);
                    break;
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    private void onConnectedPong(long pingTime, long pongTime) {
        long currentTime = System.nanoTime();
        short ping = (short) Math.max((short) (currentTime - pingTime), 0);

        this.pingTime[pingArrayIndex] = ping;
        this.clockDiff[pingArrayIndex] = pongTime - (currentTime / 2 + pingTime / 2);

        lowestPing = (short) Math.min(ping, lowestPing);

        pingArrayIndex = (pingArrayIndex + 1) % PING_TIMES_ARRAY_SIZE;
    }
}
