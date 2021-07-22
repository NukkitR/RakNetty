package org.nukkit.raknetty.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.logging.ByteBufFormat;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.nukkit.raknetty.channel.*;
import org.nukkit.raknetty.channel.nio.NioRakChannel;
import org.nukkit.raknetty.channel.nio.NioRakServerChannel;
import org.nukkit.raknetty.handler.codec.OfflinePingResponder;
import org.nukkit.raknetty.handler.codec.minecraft.MinecraftOfflinePingResponder;

import java.util.concurrent.ThreadFactory;

public class BedrockForwarder {

    static final int PORT = 19132;
    static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BedrockForwarder.class);

    static RakChannel clientChannel;
    static RakChannel serverChildChannel;
    static RakServerChannel serverChannel;

    static {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public static void main(String[] args) throws Exception {
        startClient();
        startServer();
    }

    public static void startServer() throws Exception {
        final ThreadFactory acceptFactory = new DefaultThreadFactory("accept");
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup acceptGroup = new NioEventLoopGroup(2, acceptFactory);
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(connectFactory);

        final ServerBootstrap boot = new ServerBootstrap();
        boot.group(acceptGroup, connectGroup)
                .channel(NioRakServerChannel.class)
                .option(RakServerChannelOption.RAKNET_GUID, 123456L)
                // consist with the bedrock RakNet configuration
                .option(RakServerChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
                .option(RakServerChannelOption.RAKNET_MAX_CONNECTIONS, 1)
                .option(RakServerChannelOption.RAKNET_MTU_SIZES, new int[]{1400})
                .handler(new LoggingHandler("RakServerLogger", LogLevel.INFO, ByteBufFormat.SIMPLE))
                .childHandler(new ChannelInitializer<RakChannel>() {
                    @Override
                    public void initChannel(final RakChannel ch) throws Exception {
                        serverChildChannel = ch;
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // use RakNetty client to send, use assumed priority and reliability
                                LOGGER.debug("READ: from client");
                                ReliabilityByteEnvelop envelop = (ReliabilityByteEnvelop) msg;
                                clientChannel.write(envelop);
                            }
                        });
                        ch.pipeline().addLast(new LoggingHandler("ChannelLogger", LogLevel.INFO, ByteBufFormat.SIMPLE));
                        ch.closeFuture().addListener((ChannelFutureListener) future -> serverChannel.close().sync());
                    }
                });
        // Start the server.
        final ChannelFuture future = boot.bind(PORT).sync();
        LOGGER.info("RakNetty server is ready.");

        serverChannel = (NioRakServerChannel) future.channel();
        serverChannel.closeFuture().addListener((ChannelFutureListener) future1 -> {
            LOGGER.info("RakNetty server is closed.");
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
            clientChannel.close().sync();
        });

        // Setup the offline responder
        final OfflinePingResponder responder = new MinecraftOfflinePingResponder(serverChannel, null)
                .serverName("Bedrock Server by RakNetty")
                .protocolVersion(440)
                .gameVersion("1.17.2")
                .playerCount(0)
                .levelName("Bedrock level")
                .gamemodeName("Survival")
                .gamemodeId(1)
                .build();
        serverChannel.config().setOfflinePingResponder(responder);
    }

    public static void startClient() throws Exception {
        final NioEventLoopGroup workGroup = new NioEventLoopGroup();

        final Bootstrap boot = new Bootstrap();
        boot.group(workGroup)
                .channel(NioRakChannel.class)
                .option(RakChannelOption.RAKNET_GUID, 654321L)
                // consist with the bedrock RakNet configuration
                .option(RakChannelOption.RAKNET_CONNECT_INTERVAL, 500)
                .option(RakChannelOption.RAKNET_CONNECT_ATTEMPTS, 12)
                .option(RakChannelOption.RAKNET_NUMBER_OF_INTERNAL_IDS, 20)
                .handler(new ChannelInitializer<NioRakChannel>() {
                    @Override
                    protected void initChannel(NioRakChannel ch) throws Exception {
                        ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                // use RakNetty client to send, use assumed priority and reliability
                                LOGGER.debug("READ: from remote server");
                                ReliabilityByteEnvelop envelop = (ReliabilityByteEnvelop) msg;
                                serverChildChannel.write(envelop);
                            }
                        });
                        ch.pipeline().addLast(new LoggingHandler("RakLogger", LogLevel.INFO, ByteBufFormat.SIMPLE));
                    }
                });
        // Start the server.
        final ChannelFuture future = boot.connect("kk.rekonquer.com", 19132).sync();
        LOGGER.info("RakNetty client is connected successfully.");

        clientChannel = (RakChannel) future.channel();
        clientChannel.closeFuture().addListener((ChannelFutureListener) future1 -> {
            LOGGER.info("RakNetty client is closed.");
            workGroup.shutdownGracefully();
            serverChannel.close().sync();
        });
    }
}
