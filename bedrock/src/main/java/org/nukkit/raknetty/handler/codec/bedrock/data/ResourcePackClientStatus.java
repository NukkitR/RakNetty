package org.nukkit.raknetty.handler.codec.bedrock.data;

public enum ResourcePackClientStatus {

    NONE,
    REFUSED,
    DOWNLOADING,
    DOWNLOAD_FINISHED,
    READY;

    public static ResourcePackClientStatus valueOf(int index) {
        if (index < 0 || index > ResourcePackClientStatus.values().length) return null;
        return ResourcePackClientStatus.values()[index];
    }


}
