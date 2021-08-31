package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import org.nukkit.raknetty.handler.codec.ByteBufSerializable;
import org.nukkit.raknetty.util.BinaryUtil;
import org.nukkit.raknetty.util.VarIntUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class BedrockPacketUtil extends BinaryUtil {

    public static final int PROTOCOL_NETWORK_VERSION = 448;

    private static final Logger LOGGER = LoggerFactory.getLogger(BedrockPacketUtil.class);

    public static String readString(ByteBuf buf) {
        return BinaryUtil.readString(buf,
                () -> VarIntUtil.readUnsignedVarInt(buf));
    }

    public static void writeString(ByteBuf buf, String str) {
        BinaryUtil.writeString(buf, str,
                strLen -> VarIntUtil.writeUnsignedVarInt(buf, strLen));
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, () -> VarIntUtil.readUnsignedVarInt(buf));
    }

    public static byte[] readByteArray(ByteBuf buf, Supplier<Number> lengthSupplier) {
        int len = lengthSupplier.get().intValue();
        byte[] dst = new byte[len];
        buf.readBytes(dst);
        return dst;
    }

    public static void writeBytes(ByteBuf buf, byte[] data) {
        writeBytes(buf, data, len -> VarIntUtil.writeUnsignedVarInt(buf, len));
    }

    public static void writeBytes(ByteBuf buf, byte[] data, Consumer<Integer> lengthConsumer) {
        lengthConsumer.accept(data.length);
        buf.writeBytes(data);
    }

    public static <T extends ByteBufSerializable> List<T> readList(ByteBuf buf, Class<T> typeClass, Supplier<Number> lengthSupplier) {
        return safeRead(buf, () -> {
            int length = lengthSupplier.get().intValue();
            List<T> list = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                T t = typeClass.getDeclaredConstructor().newInstance();
                t.decode(buf);
                list.add(t);
            }
            return list;
        });
    }

    public static void writeList(ByteBuf buf, Collection<? extends ByteBufSerializable> array, Consumer<Integer> lengthConsumer) {
        safeWrite(buf, () -> {
            lengthConsumer.accept(array.size());
            for (ByteBufSerializable item : array) {
                item.encode(buf);
            }
        });
    }

    private static void safeWrite(ByteBuf buf, UnsafeWriteFunction writeFunction) {
        int writerIndex = buf.writerIndex();
        try {
            writeFunction.write();
        } catch (Exception e) {
            buf.writerIndex(writerIndex);
            throw new IllegalStateException(e);
        }
    }

    private static <R> R safeRead(ByteBuf buf, UnsafeReadFunction<R> readFunction) {
        int readerIndex = buf.readerIndex();
        try {
            return readFunction.read();
        } catch (Exception e) {
            buf.readerIndex(readerIndex);
            throw new IllegalStateException(e);
        }
    }

    @FunctionalInterface
    private interface UnsafeWriteFunction {
        void write() throws Exception;
    }

    @FunctionalInterface
    private interface UnsafeReadFunction<R> {
        R read() throws Exception;
    }

}
