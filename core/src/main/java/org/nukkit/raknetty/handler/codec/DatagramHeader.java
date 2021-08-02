package org.nukkit.raknetty.handler.codec;

import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.builder.ToStringBuilder;

public final class DatagramHeader implements Cloneable {

    public static final int HEADER_LENGTH_BYTES = 2 + 3 + 4; // 2 + 3 + sizeof(float) * 1

    public enum Type {
        NAK,
        ACK,
        DATA
    }

    public static DatagramHeader getHeader(Type type) {
        switch (type) {
            case NAK:
                return new DatagramHeader(0xA0);
            case ACK:
                return new DatagramHeader(0xC0);
            case DATA:
                return new DatagramHeader(0x80);
            default:
                return null;
        }
    }

    public boolean isValid;
    public boolean isAck;
    public boolean isNak;
    public boolean isPacketPair;
    public boolean hasBandAs;
    public boolean isContinuousSend;
    public boolean needsBandAs;

    // optional field
    public float As;
    public int datagramNumber = -1;

    public DatagramHeader() {

    }

    public DatagramHeader(int header) {
        decode((byte) header);
    }

    private byte encode() {
        byte header = 0;

        // isValid always true when encoding
        header |= (1 << 7);

        if (isAck) {
            header |= (1 << 6);
            if (hasBandAs) {
                header |= (1 << 5);
            }

        } else if (isNak) {
            header |= (1 << 5);

        } else {
            if (isPacketPair) header |= (1 << 4);
            if (isContinuousSend) header |= (1 << 3);
            if (needsBandAs) header |= (1 << 2);
        }
        return header;
    }

    public void encode(ByteBuf buf) {
        byte header = encode();

        buf.writeByte(header);

        if (isAck && hasBandAs) {
            buf.writeFloat(As);
        }

        if (!isAck && !isNak) {
            buf.writeMediumLE(datagramNumber);
        }
    }

    private void decode(byte header) {
        isValid = (header & (1 << 7)) > 0;
        isAck = (header & (1 << 6)) > 0;

        if (isAck) {
            isNak = false;
            isPacketPair = false;
            hasBandAs = (header & (1 << 5)) > 0;
        } else {
            isNak = (header & (1 << 5)) > 0;

            if (isNak) {
                isPacketPair = false;

            } else {
                isPacketPair = (header & (1 << 4)) > 0;
                isContinuousSend = (header & (1 << 3)) > 0;
                needsBandAs = (header & (1 << 2)) > 0;
            }
        }
    }

    public void decode(ByteBuf buf) {
        byte header = buf.readByte();
        decode(header);

        if (isAck && hasBandAs) {
            As = buf.readFloat();
        }

        if (!isAck && !isNak) {
            datagramNumber = buf.readMediumLE();
        }
    }

    @Override
    protected DatagramHeader clone() throws CloneNotSupportedException {
        return (DatagramHeader) super.clone();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("isValid", isValid)
                .append("isAck", isAck)
                .append("isNak", isNak)
                .append("isPacketPair", isPacketPair)
                .append("hasBandAs", hasBandAs)
                .append("isContinuousSend", isContinuousSend)
                .append("needsBandAs", needsBandAs)
                .append("As", As)
                .append("datagramNumber", datagramNumber)
                .toString();
    }
}