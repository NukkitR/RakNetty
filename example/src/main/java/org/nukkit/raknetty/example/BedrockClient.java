package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.SharedConstants;
import org.nukkit.network.ClientBedrockPacketHandler;
import org.nukkit.network.channel.BedrockChannel;
import org.nukkit.network.channel.BedrockChannelOption;
import org.nukkit.network.channel.NioBedrockChannel;

public class BedrockClient {

    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BedrockClient.class);

    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static void main(String[] args) throws Exception {
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        // Configure the client.
        try {
            final Bootstrap boot = new Bootstrap();
            boot.group(workGroup)
                    .channel(NioBedrockChannel.class)
                    .option(BedrockChannelOption.BEDROCK_IS_ONLINE, false)
                    .option(BedrockChannelOption.BEDROCK_PROTOCOL_VERSION, SharedConstants.NETWORK_PROTOCOL_VERSION)
                    .option(BedrockChannelOption.BEDROCK_USERNAME, "Alex")
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast(new ClientBedrockPacketHandler());
                            ch.pipeline().addLast(new LoggingHandler("RakLogger", LogLevel.INFO));
                        }
                    });

            // Connect the client to the server
            final ChannelFuture future = boot.connect("localhost", 19132).sync();
            LOGGER.info("RakNetty client is connected successfully.");

            // Wait for the client to log in
            BedrockChannel channel = (BedrockChannel) future.channel();
            channel.loginFuture().sync();
            LOGGER.info("RakNetty client is logged in.");

            // Disconnect the client from the server after a few seconds
            Thread.sleep(20 * 1000);
            channel.disconnect().sync();
            LOGGER.info("RakNetty client is disconnected.");

            // Wait until the client socket is closed.
            channel.closeFuture().sync();
            LOGGER.info("RakNetty client is closed.");

        } finally {
            // Shut down all event loops to terminate all threads.
            workGroup.shutdownGracefully();
        }
    }
}
