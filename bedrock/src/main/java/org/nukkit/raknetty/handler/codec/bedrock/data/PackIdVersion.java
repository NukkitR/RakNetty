package org.nukkit.raknetty.handler.codec.bedrock.data;

import java.util.UUID;

public class PackIdVersion {

    protected UUID packId;
    protected SemVersion version;

    public PackIdVersion(String string) {
        String[] parts = string.split("_");
        packId = UUID.fromString(parts[0]);
        version = new SemVersion(parts[1]);
    }

    public PackIdVersion(UUID packId, SemVersion version) {
        this.packId = packId;
        this.version = version;
    }

    public final UUID packId() {
        return packId;
    }

    public final SemVersion version() {
        return version;
    }

    @Override
    public String toString() {
        return packId.toString() + "_" + version.toString();
    }
}
