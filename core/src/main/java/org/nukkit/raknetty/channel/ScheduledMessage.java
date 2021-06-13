package org.nukkit.raknetty.channel;

import org.nukkit.raknetty.handler.codec.Message;

import java.util.concurrent.TimeUnit;

public class ScheduledMessage {

    private final Message message;
    private long nanoTime;

    public ScheduledMessage(Message message) {
        this(message, 0, TimeUnit.NANOSECONDS);
    }

    public ScheduledMessage(Message message, long time, TimeUnit unit) {
        this.message = message;
        this.nanoTime = unit.toNanos(time);
    }

    public long nanoTime() {
        return nanoTime;
    }

    public Message message() {
        return message;
    }

    public ScheduledMessage nanoTime(long time, TimeUnit unit) {
        this.nanoTime = unit.toNanos(time);
        return this;
    }
}
