package org.nukkit.raknetty.handler.codec.minecraft;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.DefaultOfflinePingResponder;

import java.nio.charset.StandardCharsets;

public class MinecraftOfflinePingResponder extends DefaultOfflinePingResponder {

    private boolean isEducation;
    private String serverName;
    private int protocolVersion;
    private String gameVersion;
    private int playerCount;
    private int maxPlayer;
    private long serverId;
    private String levelName;
    private String gamemodeName;
    private int gamemodeId;
    private int port4;
    private int port6;

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
        return maxPlayer;
    }

    public MinecraftOfflinePingResponder maxPlayer(int maxPlayer) {
        this.maxPlayer = maxPlayer;
        return this;
    }

    public long serverId() {
        return serverId;
    }

    public MinecraftOfflinePingResponder serverId(long serverId) {
        this.serverId = serverId;
        return this;
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
        return port4;
    }

    public MinecraftOfflinePingResponder port4(int port4) {
        this.port4 = port4;
        return this;
    }

    public int port6() {
        return port6;
    }

    public MinecraftOfflinePingResponder port6(int port6) {
        this.port6 = port6;
        return this;
    }

    public MinecraftOfflinePingResponder build() {
        buf.clear();
        String prefix = isEducation ? "MCEE" : "MCPE";

        String str = String.format("%s;%s;%d;%s;%d;%d;%d;%s;%s;%d;%d;%d;"
                , prefix
                , serverName
                , protocolVersion
                , gameVersion
                , playerCount
                , maxPlayer
                , serverId
                , levelName
                , gamemodeName
                , gamemodeId
                , port4
                , port6
        );
        buf.writeCharSequence(str, StandardCharsets.UTF_8);
        return this;
    }

    @Override
    public ByteBuf response() {
        return super.response();
    }
}
