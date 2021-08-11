package org.nukkit.raknetty.channel;

import io.netty.channel.ChannelOption;

public class BedrockChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Boolean> BEDROCK_IS_ONLINE = ChannelOption.valueOf("BEDROCK_IS_ONLINE");

    @SuppressWarnings("deprecation")
    protected BedrockChannelOption() {
        super(null);
    }
}
