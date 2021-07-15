package org.nukkit.raknetty.handler.codec.minecraft;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.DefaultOfflinePingResponder;

import java.nio.charset.StandardCharsets;

public class MinecraftOfflinePingResponder extends DefaultOfflinePingResponder {

    private final RakServerChannel channel4;
    private final RakServerChannel channel6;

    private boolean isEducation;
    private String serverName;
    private int protocolVersion;
    private String gameVersion;
    private int playerCount;
    //private int maxPlayer;
    //private long serverId;
    private String levelName;
    private String gamemodeName;
    private int gamemodeId;
    //private int port4;
    //private int port6;

    public MinecraftOfflinePingResponder(RakServerChannel channel4) {
        this(channel4, null);
    }

    public MinecraftOfflinePingResponder(RakServerChannel channel4, RakServerChannel channel6) {
        this.channel4 = channel4;
        this.channel6 = channel6;
    }

    public MinecraftOfflinePingResponder isEducation(boolean isEducation) {
        this.isEducation = isEducation;
        return this;
    }

    public String serverName() {
        return serverName;
    }

    public MinecraftOfflinePingResponder serverName(String name) {
        this.serverName = name;
        return this;
    }

    public int protocolVersion() {
        return protocolVersion;
    }

    public MinecraftOfflinePingResponder protocolVersion(int protocolVersion) {
        this.protocolVersion = protocolVersion;
        return this;
    }

    public String gameVersion() {
        return gameVersion;
    }

    public MinecraftOfflinePingResponder gameVersion(String gameVersion) {
        this.gameVersion = gameVersion;
        return this;
    }

    public int playerCount() {
        return playerCount;
    }

    public MinecraftOfflinePingResponder playerCount(int playerCount) {
        this.playerCount = playerCount;
        return this;
    }

    public int maxPlayer() {
        return channel4.config().getMaximumConnections();
    }

    public long serverId() {
        return channel4.localGuid();
    }

    public String levelName() {
        return levelName;
    }

    public MinecraftOfflinePingResponder levelName(String levelName) {
        this.levelName = levelName;
        return this;
    }

    public String gamemodeName() {
        return gamemodeName;
    }

    public MinecraftOfflinePingResponder gamemodeName(String gamemodeName) {
        this.gamemodeName = gamemodeName;
        return this;
    }

    public int gamemodeId() {
        return gamemodeId;
    }

    public MinecraftOfflinePingResponder gamemodeId(int gamemodeId) {
        this.gamemodeId = gamemodeId;
        return this;
    }

    public int port4() {
        if (channel4 == null) return 0;
        return channel4.localAddress().getPort();
    }

    public int port6() {
        if (channel6 == null) return 0;
        return channel6.localAddress().getPort();
    }

    public MinecraftOfflinePingResponder build() {
        buf.clear();
        String prefix = isEducation ? "MCEE" : "MCPE";

        String str = String.format("%s;%s;%d;%s;%d;%d;%d;%s;%s;%d;%d;%d;"
                , prefix
                , serverName()
                , protocolVersion()
                , gameVersion()
                , playerCount()
                , maxPlayer()
                , serverId()
                , levelName()
                , gamemodeName()
                , gamemodeId()
                , port4()
                , port6()
        );

        buf.writeZero(2);
        int length = buf.writeCharSequence(str, StandardCharsets.UTF_8);
        buf.markWriterIndex();
        buf.writerIndex(0);
        buf.writeShort(length);
        buf.resetWriterIndex();
        return this;
    }

    @Override
    public ByteBuf response() {
        return super.response();
    }
}
