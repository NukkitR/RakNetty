package org.nukkit.raknetty.handler.codec.bedrock.data;

import org.apache.maven.artifact.versioning.ComparableVersion;

public class SemVersion extends ComparableVersion {

    boolean isAny = false;

    public SemVersion(String version) {
        super("0.0.0");
        if (version.equals("*")) {
            isAny = true;
        } else {
            parseVersion(version);
        }
    }

    @Override
    public String toString() {
        if (isAny) {
            return "*";
        } else {
            return super.toString();
        }
    }
}
