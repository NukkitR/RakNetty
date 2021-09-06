package org.nukkit.raknetty.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.raknetty.channel.RakServerChannelOption;
import org.nukkit.raknetty.channel.nio.NioRakServerChannel;

import java.util.concurrent.ThreadFactory;

public class RakNettyServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "19132"));
    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(RakNettyServer.class);

    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static void main(String... args) throws Exception {
        final ThreadFactory acceptFactory = new DefaultThreadFactory("accept");
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup(1, acceptFactory);
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(connectFactory);

        // Configure the server.
        try {
            final ServerBootstrap boot = new ServerBootstrap();
            boot.group(acceptGroup, connectGroup)
                    .channel(NioRakServerChannel.class)
                    // reactor channel options
                    .option(RakServerChannelOption.RAKNET_GUID, 123456L)
                    .option(RakServerChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
                    .option(RakServerChannelOption.RAKNET_PROTOCOL_VERSION, 10)
                    .option(RakServerChannelOption.RAKNET_MAX_CONNECTIONS, 15)
                    .option(RakServerChannelOption.RAKNET_MAX_MTU_SIZE, 1400)
                    .option(RakServerChannelOption.RAKNET_OFFLINE_RESPONSE_BUILDER, new ExampleBedrockPingResponse.Builder())
                    .handler(new LoggingHandler("Reactor", LogLevel.INFO))
                    .childHandler(new LoggingHandler("Connection", LogLevel.INFO));

            // Start the server.
            final ChannelFuture future = boot.bind(PORT).sync();
            final NioRakServerChannel channel = (NioRakServerChannel) future.channel();

            // Wait until the server socket is closed.
            channel.closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
        }
    }
}
