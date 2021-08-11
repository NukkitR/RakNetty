package org.nukkit.raknetty.channel.nio;

import io.netty.handler.codec.compression.JdkZlibDecoder;
import io.netty.handler.codec.compression.JdkZlibEncoder;
import io.netty.handler.codec.compression.ZlibWrapper;
import org.nukkit.raknetty.channel.BedrockChannelConfig;
import org.nukkit.raknetty.channel.DefaultBedrockChannelConfig;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.bedrock.BatchPacketHandler;
import org.nukkit.raknetty.handler.codec.bedrock.EncryptionHandler;

import java.net.InetSocketAddress;

public class NioBedrockChannel extends NioRakChannel implements BedrockChannel {

    private boolean isEncrypted = false;

    public NioBedrockChannel() {
        this(null);
    }

    NioBedrockChannel(final RakServerChannel parent) {
        super(parent);
        config().setConnectIntervalMillis(500);
        config().setConnectAttempts(12);
        config().setMaximumNumberOfInternalIds(20);

        pipeline().addLast(new EncryptionHandler(this));
        pipeline().addLast(new JdkZlibDecoder(ZlibWrapper.NONE)); // bedrock uses raw compression - no wrap
        pipeline().addLast(new JdkZlibEncoder(ZlibWrapper.NONE, 8)); // bedrock compresses with level 8
        pipeline().addLast(new BatchPacketHandler(this));
    }

    @Override
    protected BedrockChannelConfig newConfig() {
        return new DefaultBedrockChannelConfig(this, udpChannel());
    }

    @Override
    public BedrockChannelConfig config() {
        return (BedrockChannelConfig) super.config();
    }

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
    }

    @Override
    public void enableEncryption(byte[] secret) {
        //TODO
        isEncrypted = true;
    }

    @Override
    protected NioBedrockChannel remoteAddress(InetSocketAddress address) {
        return (NioBedrockChannel) super.remoteAddress(address);
    }

    @Override
    public NioBedrockChannel remoteGuid(long remoteGuid) {
        return (NioBedrockChannel) super.remoteGuid(remoteGuid);
    }
}
