package org.nukkit.raknetty.channel.nio;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.channel.bedrock.BedrockChannel;
import org.nukkit.raknetty.channel.bedrock.BedrockChannelConfig;
import org.nukkit.raknetty.channel.bedrock.DefaultBedrockChannelConfig;
import org.nukkit.raknetty.handler.codec.bedrock.*;
import org.nukkit.raknetty.handler.codec.bedrock.data.DisconnectReason;
import org.nukkit.raknetty.handler.codec.bedrock.data.SkinData;
import org.nukkit.raknetty.handler.codec.bedrock.packet.DisconnectPacket;
import org.nukkit.raknetty.handler.codec.bedrock.packet.LoginPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyAgreement;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NioBedrockChannel extends NioRakChannel implements BedrockChannel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NioBedrockChannel.class);

    private boolean isEncrypted = false;
    private final EncryptionHandler encryptionHandler;
    private final CompressionHandler compressionHandler;
    private final BatchPacketHandler batchHandler;
    private final NetworkHandler networkHandler;
    private final KeyAgreement keyAgreement;
    private final KeyPair localKeyPair;
    private PublicKey remotePublicKey;
    private LoginPacket loginPacket;

    public NioBedrockChannel() {
        this(null);
    }

    NioBedrockChannel(final RakServerChannel parent) {
        super(parent);
        config().setConnectIntervalMillis(500);
        config().setConnectAttempts(12);
        config().setMaximumNumberOfInternalIds(20);

        pipeline().addLast("Encryption", encryptionHandler = new EncryptionHandler(this));
        // bedrock uses raw compression with level 8,
        // see CompressedNetworkPeer::receivePacket and Util::decompressRaw
        // also see CompressedNetworkPeer::sendPacket and leveldb::ZlibCompressorBase::compressImpl
        pipeline().addLast("Compression", compressionHandler = new CompressionHandler(ZlibWrapper.NONE, 8));
        pipeline().addLast("Batch", batchHandler = new BatchPacketHandler(this));
        if (isClient()) {
            networkHandler = new ClientNetworkHandlerAdapter(this);
        } else {
            networkHandler = new ServerNetworkHandlerAdapter(this);
        }
        pipeline().addLast("Network", networkHandler);

        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(new ECGenParameterSpec("secp384r1"));
            localKeyPair = keyPairGenerator.generateKeyPair();

            keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(localKeyPair.getPrivate());

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Failed to initialise crypto objects.", e);
        }
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        if (isClient()) {
            if (config().isOnlineAuthenticationEnabled()) {
                loginPacket = newOnlineLoginPacket();
            } else {
                loginPacket = newOfflineLoginPacket(remoteAddress);
            }
        }
        return super.doConnect(remoteAddress, localAddress);
    }

    @Override
    protected void doFinishConnect() {
        super.doFinishConnect();

        if (isClient()) {
            batchHandler.write(batchHandler.ctx(), loginPacket, voidPromise());
        }
    }

    protected LoginPacket newOfflineLoginPacket(SocketAddress remoteAddress) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("identity", UUID.randomUUID().toString());
        extraData.put("displayName", config().getUserName());
        String tokens = WebTokenUtil.createSelfSigned(
                extraData, localPublicKey(), localPrivateKey());

        SkinData skin = config().getSkinData();
        skin.thirdPartyName = config().getUserName();
        skin.serverAddress = remoteAddress.toString();
        String skinJwt = skin.sign(localPublicKey(), localPrivateKey());

        LoginPacket login = new LoginPacket();
        login.protocolVersion = BedrockPacketUtil.PROTOCOL_NETWORK_VERSION;
        login.tokens = tokens;
        login.skinJwt = skinJwt;

        return login;
    }

    protected LoginPacket newOnlineLoginPacket() {
        throw new UnsupportedOperationException("Online log in is not supported in console. It is suggested to implement online log-in with MSAL4j with JavaFx.");
    }

    @Override
    public ChannelFuture loginFuture() {
        return networkHandler.loginFuture();
    }

    @Override
    public ChannelFuture disconnect(DisconnectReason reason) {
        // server send DisconnectPacket to client to show a screen of message
        if (reason != null && loginFuture().isSuccess()) {
            DisconnectPacket disconnect = new DisconnectPacket();
            disconnect.reason = reason;
            batchHandler.write(batchHandler.ctx(), disconnect, voidPromise());
        }

        return super.disconnect();
    }

    @Override
    public void enableEncryption(PublicKey remotePublicKey, byte[] salt) throws InvalidKeyException, InvalidAlgorithmParameterException {
        Validate.isTrue(this.remotePublicKey == null, "channel has already been encrypted.");
        this.remotePublicKey = remotePublicKey;

        keyAgreement.doPhase(remotePublicKey, true);
        ByteBuf buf = alloc().buffer(64, 64);
        buf.writeBytes(salt);
        buf.writeBytes(keyAgreement.generateSecret());
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        ReferenceCountUtil.release(buf);

        byte[] sharedSecret = DigestUtils.sha256(bytes);
        encryptionHandler.init(sharedSecret);
        isEncrypted = true;
    }

    @Override
    public boolean isEncrypted() {
        return isEncrypted;
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
    public ECPrivateKey localPrivateKey() {
        return (ECPrivateKey) localKeyPair.getPrivate();
    }

    @Override
    public ECPublicKey localPublicKey() {
        return (ECPublicKey) localKeyPair.getPublic();
    }

    @Override
    public ECPublicKey remotePublicKey() {
        return (ECPublicKey) remotePublicKey;
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
