package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.raknetty.channel.RakChannelOption;
import org.nukkit.raknetty.channel.nio.NioRakChannel;

public class BedrockClient {

    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BedrockClient.class);

    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    }

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
                    .handler(new LoggingHandler("RakLogger", LogLevel.INFO) {
                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            super.channelInactive(ctx);
                            //LOGGER.debug(new RuntimeException());
                        }
                    });
            // Start the server.
            final ChannelFuture future = boot.connect("play.lbsg.net", 19132).sync();
            LOGGER.info("RakNetty client is connected successfully.");

            // Disconnect the client from the server after a few seconds
            Thread.sleep(8000);
            future.channel().disconnect().sync();
            LOGGER.info("RakNetty client is disconnected.");

            // Wait until the server socket is closed.
            future.channel().closeFuture().sync();
            LOGGER.info("RakNetty client is closed.");
        } finally {
            // Shut down all event loops to terminate all threads.
            workGroup.shutdownGracefully();
        }
    }
}
