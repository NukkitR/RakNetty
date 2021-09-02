package org.nukkit.raknetty.handler.codec.bedrock.serialization;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.buffer.BedrockByteBuf;

import java.util.Collection;

public class Experiments implements NetworkSerializable {

    public Collection<Experiment> experiments;
    boolean wereAnyExperimentsEverToggled;

    @Override
    public void encode(BedrockByteBuf buf) throws Exception {
        buf.writeList(experiments, buf::writeIntLE);
        buf.writeBoolean(wereAnyExperimentsEverToggled);
    }

    @Override
    public void decode(BedrockByteBuf buf) throws Exception {
        experiments = buf.readList(Experiment::new, buf::readIntLE);
        wereAnyExperimentsEverToggled = buf.readBoolean();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("experiments", experiments)
                .append("wereAnyExperimentsEverToggled", wereAnyExperimentsEverToggled)
                .toString();
    }

    public static class Experiment implements NetworkSerializable {
        public String name;
        public boolean isEnabled;

        @Override
        public void encode(BedrockByteBuf buf) throws Exception {
            buf.writeString(name);
            buf.writeBoolean(isEnabled);
        }

        @Override
        public void decode(BedrockByteBuf buf) throws Exception {
            name = buf.readString();
            isEnabled = buf.readBoolean();
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("name", name)
                    .append("isEnabled", isEnabled)
                    .toString();
        }
    }
}
