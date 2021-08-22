package org.nukkit.raknetty.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.raknetty.channel.RakServerChannelOption;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.nio.NioBedrockServerChannel;
import org.nukkit.raknetty.handler.codec.bedrock.BedrockOfflinePingResponder;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponder;

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
                    .channel(NioBedrockServerChannel.class)
                    .option(RakServerChannelOption.RAKNET_GUID, 123456L)
                    .option(RakServerChannelOption.RAKNET_MAX_CONNECTIONS, 15)
                    .handler(new LoggingHandler("RakServerLogger", LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<BedrockChannel>() {
                        @Override
                        public void initChannel(final BedrockChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler("ChannelLogger", LogLevel.DEBUG) {
                                @Override
                                public void flush(ChannelHandlerContext ctx) throws Exception {
                                    super.flush(ctx);
                                }
                            });
                        }
                    });
            // Start the server.
            final ChannelFuture future = boot.bind(PORT4).sync();
            final NioBedrockServerChannel channel = (NioBedrockServerChannel) future.channel();

            // Setup the offline responder
            final OfflinePingResponder responder = new BedrockOfflinePingResponder(channel, null)
                    .serverName("Bedrock Server by RakNetty")
                    .protocolVersion(440)
                    .gameVersion("1.17.2")
                    .playerCount(0)
                    .levelName("Bedrock level")
                    .gamemodeName("Survival")
                    .gamemodeId(1)
                    .build();
            channel.config().setOfflinePingResponder(responder);

            // Wait until the server socket is closed.
            channel.closeFuture().sync();
        } finally {
            // Shut down all event loops to terminate all threads.
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
        }
    }
}
