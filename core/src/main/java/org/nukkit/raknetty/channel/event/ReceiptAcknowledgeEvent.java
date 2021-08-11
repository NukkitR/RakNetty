package org.nukkit.raknetty.channel.event;

public final class ReceiptAcknowledgeEvent extends RakChannelEvent {

    private final AcknowledgeState state;
    private final int receiptSerial;

    public ReceiptAcknowledgeEvent(AcknowledgeState state, int receiptSerial) {
        this.state = state;
        this.receiptSerial = receiptSerial;
    }

    public AcknowledgeState state() {
        return state;
    }

    public int receiptSerial() {
        return receiptSerial;
    }

    public enum AcknowledgeState {
        ACKED,
        LOSS
    }
}
