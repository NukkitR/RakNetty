package org.nukkit.raknetty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractRakDatagramChannel extends AbstractChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractRakDatagramChannel.class);

    private final DatagramChannel udpChannel;

    public AbstractRakDatagramChannel() {
        this(null);
    }

    public AbstractRakDatagramChannel(final RakServerChannel parent) {
        this(parent, parent == null ? new NioDatagramChannel() : parent.udpChannel());
    }

    protected AbstractRakDatagramChannel(final RakServerChannel parent, final DatagramChannel udpChannel) {
        super(parent);
        this.udpChannel = udpChannel;

        // reading datagram from DatagramChannel
        if (parent() == null) {
            udpChannel().pipeline().addLast(new DatagramChannelHandler());
        }

        // passing events to RakChannel
        pipeline().addLast(new RakChannelOutbound());
    }

    protected abstract boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception;

    protected abstract void doFinishConnect();

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        udpChannel().bind(localAddress);
    }

    @Override
    protected void doRegister() throws Exception {
        // register the udp channel
        if (!udpChannel().isRegistered())
            eventLoop().register(udpChannel());
    }

    @Override
    protected void doDeregister() throws Exception {
        if (parent() == null) {
            udpChannel().deregister();
        }
    }

    @Override
    protected final void doBeginRead() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void doWrite(ChannelOutboundBuffer in) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final boolean isCompatible(EventLoop loop) {
        return loop instanceof NioEventLoop;
    }

    @Override
    public final boolean isWritable() {
        return udpChannel().isWritable();
    }

    @Override
    public final long bytesBeforeWritable() {
        return udpChannel().bytesBeforeWritable();
    }

    @Override
    public final long bytesBeforeUnwritable() {
        return udpChannel().bytesBeforeUnwritable();
    }

    @Override
    public NioEventLoop eventLoop() {
        return (NioEventLoop) super.eventLoop();
    }

    public final DatagramChannel udpChannel() {
        return udpChannel;
    }

    @Override
    protected final SocketAddress localAddress0() {
        return localAddress();
    }

    @Override
    public final InetSocketAddress localAddress() {
        return udpChannel().localAddress();
    }

    @Override
    protected final SocketAddress remoteAddress0() {
        return remoteAddress();
    }

    private class DatagramChannelHandler extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            pipeline().fireChannelRead(msg);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (msg instanceof AddressedOfflineMessage) {
                AddressedOfflineMessage message = (AddressedOfflineMessage) msg;
                OfflineMessage content = message.content();

                ByteBuf buf = alloc().ioBuffer();
                content.getId().writeTo(buf);
                content.encode(buf);

                msg = new DatagramPacket(buf, message.recipient(), message.sender());
            }

            ctx.write(msg, promise);
        }
    }

    private class RakChannelOutbound extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            udpChannel().write(msg, udpChannel().newPromise().addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    promise.setSuccess();
                } else {
                    promise.setFailure(future.cause());
                }
            }));
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            udpChannel().flush();
        }
    }
}
