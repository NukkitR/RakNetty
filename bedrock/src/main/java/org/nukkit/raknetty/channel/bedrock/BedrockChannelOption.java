package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelOption;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.SerializedSkin;

public class BedrockChannelOption<T> extends ChannelOption<T> {

    public static final ChannelOption<Boolean> BEDROCK_IS_ONLINE = valueOf("BEDROCK_IS_ONLINE");
    public static final ChannelOption<String> BEDROCK_USERNAME = valueOf("BEDROCK_USERNAME");
    public static final ChannelOption<SerializedSkin> BEDROCK_SKIN_DATA = valueOf("BEDROCK_SKIN_DATA");

    @SuppressWarnings("deprecation")
    protected BedrockChannelOption() {
        super(null);
    }
}
