package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelOption;

public class BedrockChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Boolean> BEDROCK_IS_ONLINE = ChannelOption.valueOf("BEDROCK_IS_ONLINE");
    public static final ChannelOption<String> BEDROCK_USERNAME = ChannelOption.valueOf("BEDROCK_USERNAME");

    @SuppressWarnings("deprecation")
    protected BedrockChannelOption() {
        super(null);
    }
}
