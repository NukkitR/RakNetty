package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelOption;
import org.nukkit.raknetty.handler.codec.bedrock.data.SkinData;

public class BedrockChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Boolean> BEDROCK_IS_ONLINE = ChannelOption.valueOf("BEDROCK_IS_ONLINE");
    public static final ChannelOption<String> BEDROCK_USERNAME = ChannelOption.valueOf("BEDROCK_USERNAME");
    public static final ChannelOption<SkinData> BEDROCK_SKIN_DATA = ChannelOption.valueOf("BEDROCK_SKIN_DATA");

    @SuppressWarnings("deprecation")
    protected BedrockChannelOption() {
        super(null);
    }
}
