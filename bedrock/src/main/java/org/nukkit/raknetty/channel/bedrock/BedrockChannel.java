package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelFuture;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.handler.codec.bedrock.DisconnectReason;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface BedrockChannel extends RakChannel {

    boolean isEncrypted();

    void enableEncryption(PublicKey remotePublicKey, byte[] salt) throws InvalidKeyException, InvalidAlgorithmParameterException;

    ChannelFuture disconnect(DisconnectReason reason);

    ChannelFuture loginFuture();

    PrivateKey localPrivateKey();

    PublicKey localPublicKey();

    PublicKey remotePublicKey();

    @Override
    BedrockChannelConfig config();


}
