package org.nukkit.raknetty.handler.codec.reliability;

public class AcknowledgeEvent {

    private final AcknowledgeState state;
    private final int receiptSerial;

    public AcknowledgeEvent(AcknowledgeState state, int receiptSerial) {
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
        RECEIPT_ACKED,
        RECEIPT_LOSS
    }
}
