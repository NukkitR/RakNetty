package org.nukkit.raknetty.handler.codec.offline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.AddressedMessage;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessage;
import org.nukkit.raknetty.util.PacketUtil;

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
        boolean release = true;

        try {
            if (msg instanceof DatagramPacket) {
                DatagramPacket packet = (DatagramPacket) msg;
                ByteBuf buf = packet.content();
                InetSocketAddress sender = packet.sender();

                MessageIdentifier id = PacketUtil.getMessageIdentifier(buf);
                if (id == null) {
                    // it is not an offline message, proceed to the next handler
                    release = false;
                    ctx.fireChannelRead(msg);
                    return;
                }

                OfflineMessage in = null;
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
                        in = new ConnectionAttemptFailed();
                        break;
                    case ID_NO_FREE_INCOMING_CONNECTIONS:
                        in = new NoFreeIncomingConnection();
                        break;
                    case ID_CONNECTION_BANNED:
                        in = new ConnectionBanned();
                        break;
                    case ID_ALREADY_CONNECTED:
                        in = new AlreadyConnected();
                        break;
                    case ID_IP_RECENTLY_CONNECTED:
                        in = new IpRecentlyConnected();
                        break;
                    default:
                        break;
                }

                if (in != null) {
                    // an offline message, decode it and process it
                    in.decode(buf);
                    readOfflinePacket(ctx, in, sender);
                }
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.debug("Exception caught when handling a message", cause);
    }

    public abstract void readOfflinePacket(ChannelHandlerContext ctx, OfflineMessage packet, InetSocketAddress sender);

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof AddressedMessage) {
            AddressedMessage message = (AddressedMessage) msg;
            ByteBuf buf = channel.alloc().ioBuffer();
            message.content().encode(buf);
            msg = new DatagramPacket(buf, message.recipient(), message.sender());
        }

        ctx.write(msg, promise);
    }
}
