import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.commons.lang3.Range;
import org.junit.Assert;
import org.junit.Test;
import org.nukkit.raknetty.handler.codec.reliability.AcknowledgePacket;

import java.util.Arrays;

public class AcknowledgementTest {

    public static int[] INDICES = {0, 2, 3, 4, 7, 9, 10, 11, 15, 16, 50};
    public static Range<Integer>[] RANGES = new Range[]{
            Range.between(0, 0),
            Range.between(2, 4),
            Range.between(7, 7),
            Range.between(9, 11),
            Range.between(15, 16),
            Range.between(50, 50)
    };

    @Test
    public void testEncodeDecode() {
        AcknowledgePacket ACK = new AcknowledgePacket();
        Arrays.stream(INDICES).forEach(ACK::add);
        Assert.assertFalse(ACK.isEmpty());

        ByteBuf buf = Unpooled.buffer();
        ACK.encode(buf, -1, true);
        Assert.assertTrue(ACK.isEmpty());
        Assert.assertEquals(RANGES.length, buf.getShort(0));

        ACK.decode(buf);
        Assert.assertEquals(INDICES.length, ACK.indices().size());
    }

    @Test
    public void testEncodeDecodeWithLimit() {
        AcknowledgePacket ACK = new AcknowledgePacket();
        Arrays.stream(INDICES).forEach(ACK::add);
        Assert.assertFalse(ACK.isEmpty());

        ByteBuf buf = Unpooled.buffer();
        ACK.encode(buf, 13, true); // 2 + 4 + 7
        Assert.assertFalse(ACK.isEmpty());
        Assert.assertEquals(7, ACK.indices().size()); // 11 - 4
        Assert.assertEquals(2, buf.getShort(0));

        ACK.decode(buf);
        Assert.assertEquals(4, ACK.indices().size());
    }
}
