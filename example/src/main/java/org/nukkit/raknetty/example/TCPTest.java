package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.nio.charset.StandardCharsets;

public class TCPTest {

    static final int PORT = 25560;
    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(TCPTest.class);

    public static void main(String[] args) throws Exception {
        startServer();
        startClient();
    }

    public static void startServer() throws Exception {
        // Configure the server.
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();

        final ServerBootstrap boot = new ServerBootstrap();
        boot.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .handler(new LoggingHandler("ServerLogger", LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(final SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler("ServerChannelLogger", LogLevel.INFO));

                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String message = ((ByteBuf) msg).toString(StandardCharsets.UTF_8);
                                LOGGER.debug("MESSAGE: {}", msg);
                                if (message.equals("CLOSE")) {
                                    ctx.channel().close();
                                }
                            }
                        });
                    }
                });
        // Start the server.
        boot.bind(PORT).sync();
    }

    public static void startClient() throws Exception {
        final Bootstrap boot = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        boot.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler("ClientChannelLogger", LogLevel.INFO));
                    }
                });

        ChannelFuture f = boot
                .connect("127.0.0.1", PORT)
                .sync();
        Channel ch = f.channel();

        ch.writeAndFlush(Unpooled.copiedBuffer("MSG1", StandardCharsets.UTF_8)).sync();
        Thread.sleep(200);
        ch.writeAndFlush(Unpooled.copiedBuffer("MSG2", StandardCharsets.UTF_8)).sync();
        Thread.sleep(200);
        //ch.writeAndFlush(Unpooled.copiedBuffer("CLOSE", StandardCharsets.UTF_8)).sync();

        ch.disconnect();
    }
}
