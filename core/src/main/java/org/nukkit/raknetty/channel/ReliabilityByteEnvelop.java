package org.nukkit.raknetty.channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.DefaultByteBufHolder;
import org.nukkit.raknetty.handler.codec.PacketPriority;
import org.nukkit.raknetty.handler.codec.PacketReliability;

public final class ReliabilityByteEnvelop extends ReliabilityEnvelopAdapter<ByteBuf> implements ByteBufHolder {

    private final DefaultByteBufHolder holder;

    public ReliabilityByteEnvelop(ByteBuf message, PacketPriority priority, PacketReliability reliability, int orderingChannel) {
        super(message, priority, reliability, orderingChannel);
        this.holder = new DefaultByteBufHolder(message);
    }

    @Override
    public ByteBuf content() {
        return holder.content();
    }

    @Override
    public ByteBufHolder copy() {
        return holder.copy();
    }

    @Override
    public ByteBufHolder duplicate() {
        return holder.duplicate();
    }

    @Override
    public ByteBufHolder retainedDuplicate() {
        return holder.retainedDuplicate();
    }

    @Override
    public ByteBufHolder replace(ByteBuf content) {
        return holder.replace(content);
    }

    @Override
    public int refCnt() {
        return holder.refCnt();
    }

    @Override
    public ByteBufHolder retain() {
        holder.retain();
        return this;
    }

    @Override
    public ByteBufHolder retain(int increment) {
        holder.retain(increment);
        return this;
    }

    @Override
    public ByteBufHolder touch() {
        holder.touch();
        return this;
    }

    @Override
    public ByteBufHolder touch(Object hint) {
        holder.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return holder.release();
    }

    @Override
    public boolean release(int decrement) {
        return holder.release(decrement);
    }
}
