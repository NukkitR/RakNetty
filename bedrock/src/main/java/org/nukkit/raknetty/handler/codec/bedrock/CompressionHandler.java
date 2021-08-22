package org.nukkit.raknetty.handler.codec.bedrock;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.compression.DecompressionException;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionHandler extends ChannelDuplexHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(CompressionHandler.class);

    private boolean finished = false;
    private final ZlibWrapper wrapper;
    private final Inflater inflater;
    private final Deflater deflater;

    public CompressionHandler(ZlibWrapper wrapper) {
        this(wrapper, 6);
    }

    public CompressionHandler(ZlibWrapper wrapper, int level) {
        this.wrapper = wrapper;
        switch (wrapper) {
            case GZIP:
            case NONE:
                inflater = new Inflater(true);
                deflater = new Deflater(level, true);
                break;
            case ZLIB:
                inflater = new Inflater();
                deflater = new Deflater(level);
                break;
            default:
                throw new IllegalArgumentException("Only GZIP or ZLIB is supported, but you used " + wrapper);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        if (finished) {
            in.skipBytes(in.readableBytes());
            return;
        }

        int readableBytes = in.readableBytes();
        if (readableBytes == 0) {
            return;
        }

        inflater.reset();
        if (in.hasArray()) {
            inflater.setInput(in.array(), in.arrayOffset() + in.readerIndex(), readableBytes);
        } else {
            byte[] array = new byte[readableBytes];
            in.getBytes(in.readerIndex(), array);
            inflater.setInput(array);
        }

        ByteBuf decompressed = prepareDecompressBuffer(ctx, null, inflater.getRemaining() << 1);
        try {
            while (!inflater.needsInput()) {
                byte[] outArray = decompressed.array();
                int writerIndex = decompressed.writerIndex();
                int outIndex = decompressed.arrayOffset() + writerIndex;
                int outputLength = inflater.inflate(outArray, outIndex, decompressed.writableBytes());
                if (outputLength > 0) {
                    decompressed.writerIndex(writerIndex + outputLength);
                }

                if (inflater.finished()) {
                    break;
                } else {
                    decompressed = prepareDecompressBuffer(ctx, decompressed, inflater.getRemaining() << 1);
                }
            }

            in.skipBytes(readableBytes - inflater.getRemaining());

        } catch (DataFormatException e) {
            throw new DecompressionException("decompression failure", e);

        } finally {
            ReferenceCountUtil.release(in);
            ctx.fireChannelRead(decompressed);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        if (finished) {
            ctx.write(in);
            return;
        }

        int len = in.readableBytes();
        if (len == 0) {
            ReferenceCountUtil.release(in);
            return;
        }

        ByteBuf compressed = allocateCompressBuffer(ctx, in);

        int offset;
        byte[] inAry;
        if (in.hasArray()) {
            inAry = in.array();
            offset = in.arrayOffset() + in.readerIndex();
            in.skipBytes(len);
        } else {
            inAry = new byte[len];
            offset = 0;
            in.readBytes(inAry);
        }

        deflater.reset();
        deflater.setInput(inAry, offset, len);
        deflater.finish();
        for (; ; ) {
            int numBytes;
            do {
                int writerIndex = compressed.writerIndex();
                numBytes = deflater.deflate(
                        compressed.array(), compressed.arrayOffset() + writerIndex, compressed.writableBytes());
                compressed.writerIndex(writerIndex + numBytes);
            } while (numBytes > 0);

            if (deflater.needsInput()) {
                break;
            } else {
                if (!compressed.isWritable()) {
                    compressed.ensureWritable(compressed.writerIndex());
                }
            }
        }

        ReferenceCountUtil.release(in);
        ctx.write(compressed);
    }


    protected final ByteBuf prepareDecompressBuffer(ChannelHandlerContext ctx, ByteBuf buffer, int preferredSize) {
        if (buffer == null) {
            return ctx.alloc().heapBuffer(preferredSize);
        }

        if (buffer.ensureWritable(preferredSize, true) == 1) {
            buffer.skipBytes(buffer.readableBytes());
            throw new DecompressionException("Decompression buffer has reached maximum size: " + buffer.maxCapacity());
        }

        return buffer;
    }

    protected final ByteBuf allocateCompressBuffer(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        int sizeEstimate = (int) Math.ceil(msg.readableBytes() * 1.001) + 12;
        switch (wrapper) {
            case GZIP:
                sizeEstimate += 10;
                break;
            case ZLIB:
                sizeEstimate += 2;
                break;
            default:
                break;
        }

        return ctx.alloc().heapBuffer(sizeEstimate);
    }

    @Override
    public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

        if (!finished) {
            finished = true;
            inflater.end();
            deflater.end();
        }

        super.close(ctx, promise);
    }
}

