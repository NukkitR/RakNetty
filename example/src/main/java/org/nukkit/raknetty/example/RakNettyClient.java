package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.nukkit.raknetty.channel.RakChannelOption;
import org.nukkit.raknetty.channel.nio.NioRakChannel;

public class RakNettyClient {

    public static void main(String[] args) throws Exception {
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        // Configure the server.
        try {
            final Bootstrap boot = new Bootstrap();
            boot.group(workGroup)
                    .channel(NioRakChannel.class)
                    .option(RakChannelOption.RAKNET_GUID, 123456L)
                    .handler(new LoggingHandler("RakLogger", LogLevel.INFO));
            // Start the server.
            final ChannelFuture future = boot.connect("play.lbsg.net", 19132).sync();
            // Wait until the server socket is closed.
            future.channel().closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            workGroup.shutdownGracefully();
        }
    }
}
