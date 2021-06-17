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

    private final RakChannel channel;

    public ReliabilityMessageHandler(RakChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        LOGGER.debug("READ: {}", ByteBufUtil.prettyHexDump(buf));

        try {

            MessageIdentifier id = PacketUtil.getMessageIdentifier(buf);
            if (id == null) {
                // it is not an valid in
                return;
            }

            if (channel.connectMode() == ConnectMode.UNVERIFIED_SENDER) {
                if (id == MessageIdentifier.ID_CONNECTION_REQUEST) {
                    ConnectionRequest in = new ConnectionRequest();
                    in.decode(buf);
                    channel.connectMode(ConnectMode.HANDLING_CONNECTION_REQUEST);

                    ConnectionRequestAccepted out = new ConnectionRequestAccepted();
                    out.clientAddress = channel.remoteAddress();
                    out.requestTime = in.requestTime;
                    out.replyTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                    channel.send(out, PacketPriority.IMMEDIATE_PRIORITY, PacketReliability.RELIABLE_ORDERED, 0);

                } else {
                    channel.close();

                    LOGGER.debug("Temporarily banning {} for sending bad data", channel.remoteAddress());
                    channel.parent().banList().add(channel.remoteAddress(), channel.config().getTimeout());
                }

                return;
            }

            switch (id) {
                case ID_CONNECTION_REQUEST: {
                    if (channel.connectMode() == ConnectMode.REQUESTED_CONNECTION) {
                        // TODO: parse
                    } else {
                        // TODO: reply normally, onConnectionRequest
                    }
                    break;
                }
                case ID_NEW_INCOMING_CONNECTION: {
                    if (channel.connectMode() == ConnectMode.HANDLING_CONNECTION_REQUEST) {

                        channel.connectMode(ConnectMode.CONNECTED);
                        channel.ping(PacketReliability.UNRELIABLE);

                        // TODO: parse and pass it to game
                    }
                    break;
                }
                case ID_CONNECTED_PONG: {
                    // TODO: parse and onConnectedPong
                    break;
                }
                case ID_CONNECTED_PING: {
                    // TODO: parse and reply pong
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
                    break;
            }
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

}
