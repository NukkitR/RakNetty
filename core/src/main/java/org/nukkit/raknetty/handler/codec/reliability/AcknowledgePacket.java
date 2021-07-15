package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.*;

public class AcknowledgePacket extends InternalPacket {

    private final Set<Integer> indices = new HashSet<>();

    public boolean isEmpty() {
        return indices.isEmpty();
    }

    public void add(int index) {
        indices.add(index);
    }

    public void remove(int index) {
        indices.remove(index);
    }

    public void clear() {
        indices.clear();
    }

    public void encode(ByteBuf buf, int maxBytes, boolean clearWritten) {

        if (indices.isEmpty()) {
            buf.writeShort(0);
            return;
        }

        Collection<Range<Integer>> ranges = ranges();

        short countWritten = 0;
        int bytesWritten = 0;
        // save the writer index before we write the count
        int countIndex = buf.writerIndex();
        buf.writeZero(2); // skip two bytes

        Iterator<Range<Integer>> iterator = ranges.iterator();
        while (iterator.hasNext()) {
            if (maxBytes > 0 && 2 + bytesWritten + 1 + 3 * 2 > maxBytes) {
                break;
            }

            Range<Integer> range = iterator.next();
            int min = range.getMinimum();
            int max = range.getMaximum();
            boolean maxEqualToMin = min == max;

            buf.writeBoolean(maxEqualToMin);
            bytesWritten += 1;
            buf.writeMediumLE(min);
            bytesWritten += 3;
            if (!maxEqualToMin) {
                buf.writeMediumLE(max);
                bytesWritten += 3;
            }

            countWritten++;
        }

        buf.markWriterIndex();
        buf.writerIndex(countIndex);
        buf.writeShort(countWritten);
        buf.resetWriterIndex();

        // add the unwritten indices back
        if (clearWritten && countWritten > 0) {
            indices.clear();
            iterator.forEachRemaining(range -> {
                int min = range.getMinimum();
                int max = range.getMaximum();
                for (int i = min; i <= max; i++) {
                    indices.add(i);
                }
            });
        }
    }

    public void encode(ByteBuf buf, int maxBytes) {
        encode(buf, maxBytes, false);
    }

    @Override
    public void encode(ByteBuf buf) {
        encode(buf, -1);
    }

    @Override
    public void decode(ByteBuf buf) {
        short count = buf.readShort();
        boolean maxEqualToMin;
        int min, max;

        indices.clear();
        for (int i = 0; i < count; i++) {
            maxEqualToMin = buf.readBoolean();
            min = buf.readMediumLE();

            if (!maxEqualToMin) {
                max = buf.readMediumLE();
                Validate.isTrue(min <= max, "bad ack: max < min");
            } else {
                max = min;
            }

            for (int j = min; j <= max; j++) {
                indices.add(j);
            }
        }
    }

    public Collection<Integer> indices() {
        return indices;
    }

    private Collection<Range<Integer>> ranges() {
        List<Range<Integer>> ranges = new ArrayList<>();

        TreeSet<Integer> indices = new TreeSet<>(this.indices);
        Iterator<Integer> iterator = indices.iterator();
        int lower = iterator.next();
        int upper = lower;

        while (iterator.hasNext()) {

            int index = iterator.next();

            // is continuous
            if (index == upper + 1) {
                // increase the upper
                upper++;
            } else {
                // save the previous range
                ranges.add(Range.between(lower, upper));
                // reset the lower and upper
                lower = index;
                upper = index;
            }
        }

        ranges.add(Range.between(lower, upper));
        return ranges;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ranges", ranges())
                .toString();
    }
}
