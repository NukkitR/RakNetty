package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.nukkit.raknetty.channel.RakChannelOption;
import org.nukkit.raknetty.channel.nio.NioRakChannel;

public class BedrockClient {

    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BedrockClient.class);

    public static void main(String[] args) throws Exception {
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        // Configure the server.
        try {
            final Bootstrap boot = new Bootstrap();
            boot.group(workGroup)
                    .channel(NioRakChannel.class)
                    .option(RakChannelOption.RAKNET_GUID, 654321L)
                    // consist with the bedrock RakNet configuration
                    .option(RakChannelOption.RAKNET_CONNECT_INTERVAL, 500)
                    .option(RakChannelOption.RAKNET_CONNECT_ATTEMPTS, 12)
                    .option(RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
                    .handler(new LoggingHandler("RakLogger", LogLevel.INFO));
            // Start the server.
            final ChannelFuture future = boot.connect("kk.rekonquer.com", 19132).sync();

            LOGGER.info("RakNetty client is connected successfully.");

            // Wait until the server socket is closed.
            future.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            workGroup.shutdownGracefully();
        }
    }
}
