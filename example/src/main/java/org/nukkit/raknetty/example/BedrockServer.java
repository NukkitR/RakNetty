package org.nukkit.raknetty.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.channel.RakServerChannelOption;
import org.nukkit.raknetty.channel.nio.NioRakServerChannel;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;
import org.nukkit.raknetty.handler.codec.minecraft.MinecraftOfflinePingResponder;

import java.util.concurrent.ThreadFactory;

public class BedrockServer {

    static final int PORT4 = Integer.parseInt(System.getProperty("port4", "19132"));
    static final int PORT6 = Integer.parseInt(System.getProperty("port6", "19133"));
    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BedrockServer.class);

    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static void main(String[] args) throws Exception {
        final ThreadFactory acceptFactory = new DefaultThreadFactory("accept");
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup(2, acceptFactory);
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(connectFactory);

        // Configure the server.
        try {
            final ServerBootstrap boot = new ServerBootstrap();
            boot.group(acceptGroup, connectGroup)
                    .channel(NioRakServerChannel.class)
                    .option(RakServerChannelOption.RAKNET_GUID, 123456L)
                    // consist with the bedrock RakNet configuration
                    .option(RakServerChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
                    .option(RakServerChannelOption.RAKNET_MAX_CONNECTIONS, 20)
                    .handler(new LoggingHandler("RakServerLogger", LogLevel.INFO))
                    .childHandler(new ChannelInitializer<RakChannel>() {
                        @Override
                        public void initChannel(final RakChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler("ChannelLogger", LogLevel.INFO));
                        }
                    });
            // Start the server.
            final ChannelFuture future4 = boot.bind(PORT4).sync();
            final RakServerChannel channel4 = (NioRakServerChannel) future4.channel();

            // Setup the offline responder
            final OfflinePingResponder responder = new MinecraftOfflinePingResponder(channel4, null)
                    .serverName("Bedrock Server by RakNetty")
                    .protocolVersion(440)
                    .gameVersion("1.17.2")
                    .playerCount(0)
                    .levelName("Bedrock level")
                    .gamemodeName("Survival")
                    .gamemodeId(1)
                    .build();
            channel4.config().setOfflinePingResponder(responder);

            // Wait until the server socket is closed.
            channel4.closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
        }
    }
}
