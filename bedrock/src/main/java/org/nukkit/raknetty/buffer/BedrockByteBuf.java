package org.nukkit.raknetty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.NetworkSerializable;
import org.nukkit.raknetty.handler.codec.bedrock.serialization.NetworkSerializer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BedrockByteBuf extends WrappedByteBuf {

    public static int getVarIntSize(int i) {
        return getVarIntSize0(i, 5);
    }

    public static int getVarLongSize(long i) {
        return getVarIntSize0(i, 10);
    }

    private static int getVarIntSize0(long i, int maxSize) {
        for (int size = 1; size < maxSize; ++size) {
            if ((i & -1L << size * 7) == 0) {
                return size;
            }
        }
        return maxSize;
    }

    public static BedrockByteBuf wrap(ByteBuf buf) {
        return new BedrockByteBuf(buf);
    }

    public BedrockByteBuf(ByteBuf data) {
        super(data);
    }

    public int readUnsignedVarInt() {
        return (int) readVarInt0(5, "VarInt is too big.");
    }

    public int readVarInt() {
        long value = readUnsignedVarInt() & 0xffffffffL;
        return (int) ((value >>> 1) ^ -(value & 1));
    }

    public long readUnsignedVarLong() {
        return readVarInt0(10, "VarLong is too big.");
    }

    public long readVarLong() {
        long value = readUnsignedVarLong();
        return (value >>> 1) ^ -(value & 1);
    }

    private long readVarInt0(int maxSize, String exception) {
        if (!this.isReadable()) {
            return 0;
        }

        this.markReaderIndex();

        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = this.readByte();
            long value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > maxSize) {
                this.resetReaderIndex();
                throw new RuntimeException(exception);
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public BedrockByteBuf writeUnsignedVarInt(int value) {
        writeVarInt0(value & 0xFFFFFFFFL);
        return this;
    }

    public BedrockByteBuf writeVarInt(int value) {
        value = (value << 1) ^ (value >> 31);
        writeVarInt0(value & 0xFFFFFFFFL);
        return this;
    }

    public BedrockByteBuf writeUnsignedVarLong(long value) {
        writeVarInt0(value);
        return this;
    }

    public BedrockByteBuf writeVarLong(long value) {
        value = (value << 1) ^ (value >> 63);
        writeVarInt0(value);
        return this;
    }

    private void writeVarInt0(long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            buf.writeByte(temp);
        } while (value != 0);
    }

    public byte[] readByteArray() {
        return this.readByteArray(buf.readableBytes());
    }

    public byte[] readByteArray(int maxSize) {
        int j = this.readUnsignedVarInt();
        if (j > maxSize) {
            throw new DecoderException("ByteArray with size " + j + " is bigger than allowed " + maxSize);
        } else {
            byte[] bytes = new byte[j];
            buf.readBytes(bytes);
            return bytes;
        }
    }

    public BedrockByteBuf writeByteArray(byte[] bytes) {
        this.writeUnsignedVarInt(bytes.length);
        buf.writeBytes(bytes);
        return this;
    }

    public <T extends NetworkSerializable> List<T> readList(Supplier<T> objectSupplier, Supplier<Number> lengthSupplier) {
        try {
            int length = lengthSupplier.get().intValue();
            List<T> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                T t = objectSupplier.get();
                t.decode(this);
                list.add(t);
            }
            return list;
        } catch (Exception e) {
            throw new DecoderException("Failed to read list", e);
        }
    }

    public <T> List<T> readList(NetworkSerializer<T> serializer, Supplier<Number> lengthSupplier) {
        try {
            int length = lengthSupplier.get().intValue();
            List<T> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                list.add(serializer.decode(this));
            }
            return list;
        } catch (Exception e) {
            throw new DecoderException("Failed to read list", e);
        }
    }

    public BedrockByteBuf writeList(Collection<? extends NetworkSerializable> array, Consumer<Integer> lengthConsumer) {
        try {
            lengthConsumer.accept(array.size());
            for (NetworkSerializable item : array) {
                item.encode(this);
            }
            return this;
        } catch (Exception e) {
            throw new EncoderException("Failed to write list", e);
        }
    }

    public <T> BedrockByteBuf writeList(Collection<T> array, NetworkSerializer<T> serializer, Consumer<Integer> lengthConsumer) {
        try {
            lengthConsumer.accept(array.size());
            for (T t : array) {
                serializer.encode(this, t);
            }
            return this;
        } catch (Exception e) {
            throw new EncoderException("Failed to write list", e);
        }
    }

    public <T extends Enum<T>> T readEnum(Class<T> tClass) {
        return tClass.getEnumConstants()[this.readUnsignedVarInt()];
    }

    public BedrockByteBuf writeEnum(Enum<?> value) {
        return this.writeUnsignedVarInt(value.ordinal());
    }

    public UUID readUUID() {
        return new UUID(this.readLong(), this.readLong());
    }

    public BedrockByteBuf writeUUID(UUID uuid) {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
        return this;
    }

    public String readString() {
        return readString(this.readableBytes());
    }

    public String readString(int maxLength) {
        int len = this.readUnsignedVarInt();

        if (len > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + len + " > " + maxLength * 4 + ")");
        } else if (len < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            String s = this.toString(this.readerIndex(), len, StandardCharsets.UTF_8);

            this.readerIndex(this.readerIndex() + len);
            if (s.length() > maxLength) {
                throw new DecoderException("The received string length is longer than maximum allowed (" + len + " > " + maxLength + ")");
            } else {
                return s;
            }
        }
    }

    public BedrockByteBuf writeString(String s) {
        return this.writeString(s, 32767);
    }

    public BedrockByteBuf writeString(String s, int maxLength) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);

        if (bytes.length > maxLength) {
            throw new EncoderException("String too big (was " + bytes.length + " bytes encoded, max " + maxLength + ")");
        } else {
            this.writeUnsignedVarInt(bytes.length);
            this.writeBytes(bytes);
            return this;
        }
    }

}
