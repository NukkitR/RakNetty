package org.nukkit.raknetty.channel.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.AddressedMessage;
import org.nukkit.raknetty.channel.RakServerChannel;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public abstract class AbstractNioRakChannel extends AbstractChannel {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(AbstractNioRakChannel.class);

    private final DatagramChannel udpChannel;

    public AbstractNioRakChannel() {
        this(null);
    }

    public AbstractNioRakChannel(final RakServerChannel parent) {
        this(parent, parent == null ? new NioDatagramChannel() : parent.udpChannel());
    }

    protected AbstractNioRakChannel(final RakServerChannel parent, final DatagramChannel udpChannel) {
        super(parent);
        this.udpChannel = udpChannel;

        // reading datagram from DatagramChannel
        if (parent() == null) {
            // this is a server-side channel
            udpChannel().pipeline().addLast(new DatagramChannelInbound());
        }

        // passing events to DatagramChannel
        pipeline().addLast(new NioRakChannelOutbound());
    }

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
    protected void doBeginRead() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doWrite(ChannelOutboundBuffer in) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected boolean isCompatible(EventLoop loop) {
        return loop instanceof NioEventLoop;
    }

    @Override
    public boolean isWritable() {
        return udpChannel().isWritable();
    }

    @Override
    public long bytesBeforeWritable() {
        return udpChannel().bytesBeforeWritable();
    }

    @Override
    public long bytesBeforeUnwritable() {
        return udpChannel().bytesBeforeUnwritable();
    }

    public DatagramChannel udpChannel() {
        return udpChannel;
    }

    @Override
    protected SocketAddress localAddress0() {
        return localAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return udpChannel().localAddress();
    }

    @Override
    protected SocketAddress remoteAddress0() {
        return remoteAddress();
    }

    @Override
    public InetSocketAddress remoteAddress() {
        // NOTE: should be override.
        throw new UnsupportedOperationException();
    }

    private class DatagramChannelInbound extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            pipeline().fireChannelRead(msg);
        }
    }

    private class NioRakChannelOutbound extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

            if (msg instanceof AddressedMessage) {
                AddressedMessage message = (AddressedMessage) msg;
                ByteBuf buf = alloc().ioBuffer();
                message.content().encode(buf);
                msg = new DatagramPacket(buf, message.recipient(), message.sender());
            }

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
