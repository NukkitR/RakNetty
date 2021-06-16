package org.nukkit.raknetty.handler.codec.reliability;

public class HeapedPacket {

    int weight;
    InternalPacket packet;

    public HeapedPacket(int weight, InternalPacket packet) {
        this.weight = weight;
        this.packet = packet;
    }

}
