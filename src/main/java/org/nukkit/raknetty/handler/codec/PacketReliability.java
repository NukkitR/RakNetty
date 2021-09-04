package org.nukkit.raknetty.handler.codec;

public enum PacketReliability {
    /**
     * Same as regular UDP, except that it will also discard duplicate datagrams.
     * RakNet adds (6 to 17) + 21 bits of overhead, 16 of which is used to detect duplicate packets
     * and 6 to 17 of which is used for message length.
     */
    UNRELIABLE(false, false, false, false),

    /**
     * Regular UDP with a sequence counter. Out of order messages will be discarded.
     * Sequenced and ordered messages sent on the same channel will arrive in the order sent.
     */
    UNRELIABLE_SEQUENCED(false, true, true, false),

    /**
     * The message is sent reliably, but not necessarily in any order.
     * Same overhead as {@link PacketReliability#UNRELIABLE}.
     */
    RELIABLE(true, false, false, false),

    /**
     * This message is reliable and will arrive in the order you sent it.
     * Messages will be delayed while waiting for out of order messages.
     * Same overhead as {@link PacketReliability#UNRELIABLE_SEQUENCED}.
     * Sequenced and ordered messages sent on the same channel will arrive in the order sent.
     */
    RELIABLE_ORDERED(true, true, false, false),

    /**
     * This message is reliable and will arrive in the sequence you sent it.
     * Out or order messages will be dropped.
     * Same overhead as {@link PacketReliability#UNRELIABLE_SEQUENCED}.
     * Sequenced and ordered messages sent on the same channel will arrive in the order sent.
     */
    RELIABLE_SEQUENCED(true, true, true, false),

    /**
     * Same as {@link PacketReliability#UNRELIABLE},
     * however the user will get either ID_SND_RECEIPT_ACKED or ID_SND_RECEIPT_LOSS
     * based on the result of sending this message when calling RakPeerInterface::Receive().
     * Bytes 1-4 will contain the number returned from the Send() function.
     * On disconnect or shutdown, all messages not previously acked should be considered lost.
     */
    UNRELIABLE_WITH_ACK_RECEIPT(false, false, false, true),

    /**
     * Same as {@link PacketReliability#RELIABLE}.
     * The user will also get ID_SND_RECEIPT_ACKED after the message is delivered
     * when calling RakPeerInterface::Receive().
     * ID_SND_RECEIPT_ACKED is returned when the message arrives, not necessarily the order when it was sent.
     * Bytes 1-4 will contain the number returned from the Send() function.
     * On disconnect or shutdown, all messages not previously acked should be considered lost.
     * This does not return ID_SND_RECEIPT_LOSS.
     */
    RELIABLE_WITH_ACK_RECEIPT(true, false, false, true),

    /**
     * Same as {@link PacketReliability#RELIABLE_ORDERED}.
     * The user will also get ID_SND_RECEIPT_ACKED after the message is delivered
     * when calling RakPeerInterface::Receive().
     * ID_SND_RECEIPT_ACKED is returned when the message arrives, not necessarily the order when it was sent.
     * Bytes 1-4 will contain the number returned from the Send() function.
     * On disconnect or shutdown, all messages not previously acked should be considered lost.
     * This does not return ID_SND_RECEIPT_LOSS.
     */
    RELIABLE_ORDERED_WITH_ACK_RECEIPT(true, true, false, true);

    private final boolean isReliable;
    private final boolean isOrdered;
    private final boolean isSequenced;
    private final boolean withAckReceipt;

    PacketReliability(boolean isReliable, boolean isOrdered, boolean isSequenced, boolean withAckReceipt) {
        this.isReliable = isReliable;
        this.isOrdered = isOrdered;
        this.isSequenced = isSequenced;
        this.withAckReceipt = withAckReceipt;
    }

    public boolean isReliable() {
        return isReliable;
    }

    public boolean isOrdered() {
        return isOrdered;
    }

    public boolean isSequenced() {
        return isSequenced;
    }

    public boolean withAckReceipt() {
        return withAckReceipt;
    }

    public static final PacketReliability[] RELIABILITIES = PacketReliability.values();
    public static final int NUM_OF_RELIABILITIES = RELIABILITIES.length;

    public static PacketReliability valueOf(int id) {
        if (id < 0 || id >= NUM_OF_RELIABILITIES) return null;

        return RELIABILITIES[id];
    }
}
