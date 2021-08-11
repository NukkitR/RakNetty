package org.nukkit.raknetty.util;

import io.netty.buffer.ByteBuf;

public class VarIntUtil {

    public static long readUnsignedVarInt(ByteBuf buf) {
        return read(buf, 5, "VarInt is too big.");
    }

    public static int readVarInt(ByteBuf buf) {
        long value = readUnsignedVarInt(buf);
        return (int) ((value >>> 1) ^ -(value & 1));
    }

    public static long readUnsignedVarLong(ByteBuf buf) {
        return read(buf, 10, "VarLong is too big.");
    }

    public static long readVarLong(ByteBuf buf) {
        long value = readUnsignedVarLong(buf);
        return (value >>> 1) ^ -(value & 1);
    }

    private static long read(ByteBuf buf, int maxSize, String exception) {
        if (!buf.isReadable()) {
            return 0;
        }

        buf.markReaderIndex();

        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = buf.readByte();
            long value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > maxSize) {
                buf.resetReaderIndex();
                throw new RuntimeException(exception);
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeUnsignedVarInt(ByteBuf buf, long value) {
        write(buf, value & 0xFFFFFFFFL);
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        value = (value << 1) ^ (value >> 31);
        write(buf, value & 0xFFFFFFFFL);
    }

    public static void writeUnsignedVarLong(ByteBuf buf, long value) {
        write(buf, value);
    }

    public static void writeVarLong(ByteBuf buf, long value) {
        value = (value << 1) ^ (value >> 63);
        write(buf, value);
    }

    private static void write(ByteBuf buf, long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buf.writeByte(temp);
        } while (value != 0);
    }

}
