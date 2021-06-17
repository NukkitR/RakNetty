package org.nukkit.raknetty.handler.codec;

public abstract class DefaultReliabilityMessage implements ReliabilityMessage, Cloneable {

    public PacketReliability reliability;
    public PacketPriority priority;
    public DatagramHeader header;

    public int headerLength;

    public int receiptSerial;
    public int timeSent;
    public long actionTime;
    public long retransmissionTime;
    public long creationTime = System.nanoTime();

    public abstract int bodyLength();

    @Override
    protected DefaultReliabilityMessage clone() throws CloneNotSupportedException {
        DefaultReliabilityMessage msg = (DefaultReliabilityMessage) super.clone();
        msg.creationTime = System.nanoTime();
        msg.header = header.clone();
        return msg;
    }
}
