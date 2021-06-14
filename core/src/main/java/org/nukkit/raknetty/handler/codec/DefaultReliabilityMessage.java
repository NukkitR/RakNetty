package org.nukkit.raknetty.handler.codec;

public abstract class DefaultReliabilityMessage implements ReliabilityMessage {

    public PacketReliability reliability;
    public PacketPriority priority;
    public DatagramHeader header;

    public int headerLength;

    public int receiptSerial;
    public int timeSent;
    public long actionTime;
    public long retransmissionTime;
    public final long creationTime = System.nanoTime();

    public abstract int bodyLength();
}
