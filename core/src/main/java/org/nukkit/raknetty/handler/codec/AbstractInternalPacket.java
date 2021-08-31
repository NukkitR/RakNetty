package org.nukkit.raknetty.handler.codec;

public abstract class AbstractInternalPacket implements Cloneable, ByteBufSerializable {

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
    protected AbstractInternalPacket clone() throws CloneNotSupportedException {
        AbstractInternalPacket msg = (AbstractInternalPacket) super.clone();
        msg.creationTime = System.nanoTime();
        msg.header = header.clone();
        return msg;
    }
}
