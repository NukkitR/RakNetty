package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.ReferenceCounted;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.nukkit.raknetty.handler.codec.DefaultReliabilityMessage;
import org.nukkit.raknetty.handler.codec.MTUSize;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.util.PacketUtil;

public class InternalPacket extends DefaultReliabilityMessage implements ReferenceCounted {

    public static final int NUMBER_OF_ORDERED_STREAMS = 32;

    public int splitPacketCount;
    public int splitPacketId;
    public int splitPacketIndex;

    public int reliableIndex;
    public int sequencingIndex;
    public int orderingIndex;
    public int orderingChannel;

    public ByteBuf data;

    @Override
    public void encode(ByteBuf buf) {
        // TODO:
        byte flag = 0;
        PacketReliability temp = switch (this.reliability) {
            case UNRELIABLE_WITH_ACK_RECEIPT -> PacketReliability.UNRELIABLE;
            case RELIABLE_WITH_ACK_RECEIPT -> PacketReliability.RELIABLE;
            case RELIABLE_ORDERED_WITH_ACK_RECEIPT -> PacketReliability.RELIABLE_ORDERED;
            default -> this.reliability;
        };

        flag |= temp.ordinal() << 5;

        if (hasSplitPacket()) {
            flag |= 0b1 << 4;
        }

        buf.writeByte(flag);

        int bitLength = bodyLength() * 8;
        Validate.isTrue(bitLength < 0xFFFF, "payload too large"); // data bit length should be less than 65535

        buf.writeShort(bitLength);

        if (reliability.isReliable()) {
            buf.writeMediumLE(reliableIndex);
        }

        if (reliability.isSequenced()) {
            buf.writeMediumLE(sequencingIndex);
        }

        if (reliability.isOrdered()) {
            buf.writeMediumLE(orderingIndex);
            buf.writeByte(orderingChannel);
        }

        if (hasSplitPacket()) {
            buf.writeInt(splitPacketCount);
            buf.writeShort(splitPacketId);
            buf.writeInt(splitPacketIndex);
        }

        buf.writeBytes(data);
    }

    @Override
    public void decode(ByteBuf buf) {
        byte flag = buf.readByte();
        reliability = PacketReliability.valueOf(flag >> 5);
        boolean hasSplitPacket = (flag >> 4 & 0b1) != 0;
        int bitLength = buf.readShort();

        Validate.isTrue(!reliability.withAckReceipt(), "ACK_RECEIPT from remote system");

        if (reliability.isReliable()) {
            reliableIndex = buf.readMediumLE();
        }

        if (reliability.isSequenced()) {
            sequencingIndex = buf.readMediumLE();
        }

        if (reliability.isOrdered()) {
            orderingIndex = buf.readMediumLE();
            orderingChannel = buf.readByte();
        } else {
            orderingChannel = 0;
        }

        if (hasSplitPacket) {
            splitPacketCount = buf.readInt();
            splitPacketId = buf.readShort();
            splitPacketIndex = buf.readInt();
        } else {
            splitPacketCount = 0;
        }

        // let's check if we are happy with everything
        Validate.isTrue(bitLength > 0, "bad packet bit length");
        Validate.isTrue(reliability != null, "bad packet reliability");
        Validate.isTrue(orderingChannel >= 0 && orderingChannel < NUMBER_OF_ORDERED_STREAMS, "bad channel index");
        Validate.isTrue(!hasSplitPacket || (splitPacketIndex >= 0 && splitPacketIndex < splitPacketCount), "bad split index");

        // we want to make sure the packet have enough bytes left to read
        // and it should be smaller than the MTU size
        int bodyLength = PacketUtil.bitToBytes(bitLength);
        Validate.isTrue(bodyLength < MTUSize.MAXIMUM_MTU_SIZE, "packet length exceeds the limit");
        Validate.isTrue(bodyLength <= buf.readableBytes(), "not enough bytes to read");

        data = buf.retainedSlice(buf.readerIndex(), bodyLength);
    }

    @Override
    public int bodyLength() {
        return data.writerIndex() - data.readerIndex();
    }

    public boolean hasSplitPacket() {
        return splitPacketCount > 0;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .append("reliability", reliability);

        if (hasSplitPacket()) {
            builder.append("splitPacketCount", splitPacketCount)
                    .append("splitPacketId", splitPacketId)
                    .append("splitPacketIndex", splitPacketIndex);
        }

        if (reliability.isReliable()) {
            builder.append("reliableIndex", reliableIndex);

            if (reliability.isOrdered()) {
                builder.append("orderingIndex", orderingIndex)
                        .append("orderingChannel", orderingChannel);
            }

            if (reliability.isSequenced()) builder.append("sequencingIndex", sequencingIndex);

        } else if (reliability.isSequenced()) {
            builder.append("sequencingIndex", sequencingIndex);
        }

        return builder.toString();
    }

    @Override
    public int refCnt() {
        return ReferenceCountUtil.refCnt(data);
    }

    @Override
    public ReferenceCounted retain() {
        return ReferenceCountUtil.retain(data);
    }

    @Override
    public ReferenceCounted retain(int increment) {
        return ReferenceCountUtil.retain(data, increment);
    }

    @Override
    public ReferenceCounted touch() {
        return ReferenceCountUtil.touch(data);
    }

    @Override
    public ReferenceCounted touch(Object hint) {
        return ReferenceCountUtil.touch(data, hint);
    }

    @Override
    public boolean release() {
        return ReferenceCountUtil.release(data);
    }

    @Override
    public boolean release(int decrement) {
        return ReferenceCountUtil.release(data, decrement);
    }
}
