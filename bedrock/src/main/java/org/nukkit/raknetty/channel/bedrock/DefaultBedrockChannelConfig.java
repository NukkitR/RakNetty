package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.*;
import io.netty.channel.socket.DatagramChannel;
import io.netty.util.internal.ObjectUtil;
import org.nukkit.raknetty.channel.DefaultRakChannelConfig;
import org.nukkit.raknetty.channel.RakChannelConfig;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.SerializedSkin;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponder;

import java.util.Map;


public class DefaultBedrockChannelConfig extends DefaultChannelConfig implements BedrockChannelConfig, RakChannelConfig {

    private volatile boolean isOnline = true;
    private volatile String username = "Steve";
    private volatile SerializedSkin skinData = SerializedSkin.randomSkin();

    private final RakChannelConfig rakConfig;

    public DefaultBedrockChannelConfig(Channel channel, DatagramChannel udpChannel) {
        super(channel, new FixedRecvByteBufAllocator(2048));
        this.rakConfig = newRakConfig(channel, udpChannel);
    }

    protected RakChannelConfig newRakConfig(Channel channel, DatagramChannel udpChannel) {
        return new DefaultRakChannelConfig(channel, udpChannel);
    }

    protected RakChannelConfig rakConfig() {
        return this.rakConfig;
    }

    @Override
    public Map<ChannelOption<?>, Object> getOptions() {
        return getOptions(rakConfig().getOptions(),
                BedrockChannelOption.BEDROCK_IS_ONLINE,
                BedrockChannelOption.BEDROCK_USERNAME,
                BedrockChannelOption.BEDROCK_SKIN_DATA);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getOption(ChannelOption<T> option) {
        if (option == BedrockChannelOption.BEDROCK_IS_ONLINE) {
            return (T) (Boolean) isOnlineAuthenticationEnabled();
        } else if (option == BedrockChannelOption.BEDROCK_USERNAME) {
            return (T) getUserName();
        } else if (option == BedrockChannelOption.BEDROCK_SKIN_DATA) {
            return (T) getSkinData();
        }
        return rakConfig().getOption(option);
    }

    @Override
    public <T> boolean setOption(ChannelOption<T> option, T value) {
        validate(option, value);

        if (option == BedrockChannelOption.BEDROCK_IS_ONLINE) {
            setOnlineAuthenticationEnabled((boolean) value);
        } else if (option == BedrockChannelOption.BEDROCK_USERNAME) {
            setUserName((String) value);
        } else if (option == BedrockChannelOption.BEDROCK_SKIN_DATA) {
            setSkinData((SerializedSkin) value);
        } else {
            return rakConfig().setOption(option, value);
        }

        return true;
    }

    @Override
    public boolean isOnlineAuthenticationEnabled() {
        return isOnline;
    }

    @Override
    public BedrockChannelConfig setOnlineAuthenticationEnabled(boolean isOnline) {
        this.isOnline = isOnline;
        return this;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public BedrockChannelConfig setUserName(String username) {
        ObjectUtil.checkNotNull(username, "userName");
        this.username = username;
        return this;
    }

    @Override
    public SerializedSkin getSkinData() {
        return skinData;
    }

    @Override
    public BedrockChannelConfig setSkinData(SerializedSkin skinData) {
        this.skinData = skinData;
        return this;
    }

    @Override
    public long getLocalGuid() {
        return rakConfig().getLocalGuid();
    }

    @Override
    public BedrockChannelConfig setLocalGuid(long guid) {
        rakConfig().setLocalGuid(guid);
        return this;
    }

    @Override
    public int[] getMtuSizes() {
        return rakConfig().getMtuSizes();
    }

    @Override
    public BedrockChannelConfig setMtuSizes(int[] mtuSizes) {
        rakConfig().setMtuSizes(mtuSizes);
        return this;
    }

    @Override
    public int getMaximumMtuSize() {
        return rakConfig().getMaximumMtuSize();
    }

    @Override
    public int getConnectAttempts() {
        return rakConfig().getConnectAttempts();
    }

    @Override
    public BedrockChannelConfig setConnectAttempts(int connectAttempts) {
        rakConfig().setConnectAttempts(connectAttempts);
        return this;
    }

    @Override
    public int getConnectIntervalMillis() {
        return rakConfig.getConnectIntervalMillis();
    }

    @Override
    public BedrockChannelConfig setConnectIntervalMillis(int connectIntervalMillis) {
        rakConfig().setConnectIntervalMillis(connectIntervalMillis);
        return this;
    }

    @Override
    public int getConnectTimeoutMillis() {
        return rakConfig().getConnectTimeoutMillis();
    }

    @Override
    public BedrockChannelConfig setConnectTimeoutMillis(int connectTimeoutMillis) {
        rakConfig().setConnectTimeoutMillis(connectTimeoutMillis);
        return this;
    }

    @Override
    public int getTimeoutMillis() {
        return rakConfig().getTimeoutMillis();
    }

    @Override
    public BedrockChannelConfig setTimeoutMillis(int timeoutMillis) {
        rakConfig().setTimeoutMillis(timeoutMillis);
        return this;
    }

    @Override
    public int getUnreliableTimeoutMillis() {
        return rakConfig().getUnreliableTimeoutMillis();
    }

    @Override
    public BedrockChannelConfig setUnreliableTimeoutMillis(int unreliableTimeoutMillis) {
        rakConfig().setUnreliableTimeoutMillis(unreliableTimeoutMillis);
        return this;
    }

    @Override
    public int getMaximumNumberOfInternalIds() {
        return rakConfig().getMaximumNumberOfInternalIds();
    }

    @Override
    public BedrockChannelConfig setMaximumNumberOfInternalIds(int numberOfInternalIds) {
        rakConfig().setMaximumNumberOfInternalIds(numberOfInternalIds);
        return this;
    }

    @Override
    public OfflinePingResponder getOfflinePingResponder() {
        return rakConfig().getOfflinePingResponder();
    }

    @Override
    public BedrockChannelConfig setOfflinePingResponder(OfflinePingResponder responder) {
        rakConfig().setOfflinePingResponder(responder);
        return this;
    }

    @Override
    public boolean isAutoRead() {
        return rakConfig().isAutoRead();
    }

    @Override
    public ChannelConfig setAutoRead(boolean autoRead) {
        return rakConfig().setAutoRead(autoRead);
    }
}
