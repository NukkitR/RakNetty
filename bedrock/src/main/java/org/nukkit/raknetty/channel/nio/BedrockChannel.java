package org.nukkit.raknetty.channel.nio;

import org.nukkit.raknetty.channel.BedrockChannelConfig;
import org.nukkit.raknetty.channel.RakChannel;

public interface BedrockChannel extends RakChannel {

    boolean isEncrypted();

    void enableEncryption(byte[] secret);

    @Override
    BedrockChannelConfig config();

}
