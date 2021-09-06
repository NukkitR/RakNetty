package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;

public abstract class AbstractOfflineHandler extends ChannelDuplexHandler {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractOfflineHandler.class);

    private final Channel channel;

    public AbstractOfflineHandler(Channel channel) {
        this.channel = channel;
    }

    public Channel channel() {
        return this.channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        DatagramPacket packet = (DatagramPacket) msg;
        ByteBuf buf = packet.content();
        InetSocketAddress sender = packet.sender();

        boolean isOffline = true;

        try {
            buf.markReaderIndex();
            MessageIdentifier id = MessageIdentifier.readFrom(buf);
            if (id == null) {
                // it is not an offline message, proceed to the next handler
                isOffline = false;
                return;
            }

            OfflineMessage in;
            switch (id) {
                case ID_UNCONNECTED_PING:
                    in = new UnconnectedPing();
                    break;
                case ID_UNCONNECTED_PING_OPEN_CONNECTIONS:
                    in = new UnconnectedPingOpenConnections();
                    break;
                case ID_UNCONNECTED_PONG:
                    in = new UnconnectedPong();
                    break;
                case ID_OPEN_CONNECTION_REQUEST_1:
                    in = new OpenConnectionRequest1();
                    break;
                case ID_OPEN_CONNECTION_REQUEST_2:
                    in = new OpenConnectionRequest2();
                    break;
                case ID_OPEN_CONNECTION_REPLY_1:
                    in = new OpenConnectionReply1();
                    break;
                case ID_OPEN_CONNECTION_REPLY_2:
                    in = new OpenConnectionReply2();
                    break;
                case ID_CONNECTION_ATTEMPT_FAILED:
                case ID_NO_FREE_INCOMING_CONNECTIONS:
                case ID_CONNECTION_BANNED:
                case ID_ALREADY_CONNECTED:
                case ID_INVALID_PASSWORD:
                case ID_IP_RECENTLY_CONNECTED:
                case ID_INCOMPATIBLE_PROTOCOL_VERSION:
                    connectionAttemptFailed(ctx, id);
                    return;
                default:
                    isOffline = false;
                    return;
            }

            // an offline message, decode it and process it
            in.decode(buf);
            readOfflinePacket(ctx, in, sender);
        } finally {
            if (isOffline) {
                ReferenceCountUtil.release(msg);
            } else {
                buf.resetReaderIndex();
                ctx.fireChannelRead(msg);
            }
        }
    }

    public void connectionAttemptFailed(ChannelHandlerContext ctx, MessageIdentifier reason) {
        // NOOP
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.debug("Exception caught when handling a message", cause);
    }

    public abstract void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage msg, InetSocketAddress sender);
}
