package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelOption;

public class BedrockServerChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Boolean> BEDROCK_IS_ONLINE = ChannelOption.valueOf("BEDROCK_IS_ONLINE");

    @SuppressWarnings("deprecation")
    protected BedrockServerChannelOption() {
        super(null);
    }
}
