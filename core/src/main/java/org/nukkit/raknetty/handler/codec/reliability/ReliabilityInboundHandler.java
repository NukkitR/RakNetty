package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.handler.codec.DatagramHeader;
import org.nukkit.raknetty.handler.codec.PacketReliability;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import static org.nukkit.raknetty.handler.codec.reliability.InternalPacket.NUMBER_OF_ORDERED_STREAMS;

public class ReliabilityInboundHandler extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReliabilityInboundHandler.class);
    public static final String NAME = "ReliabilityIn";

    private final RakChannel channel;
    private ReliabilityOutboundHandler out;

    /**
     * The packet number we are expecting
     */
    private int receivedBaseIndex = 0;

    /**
     * The maximum packet number we have ever received
     */
    private int receivedTopIndex = 0;

    private final PriorityQueue<HeapedPacket>[] orderingHeaps = new PriorityQueue[NUMBER_OF_ORDERED_STREAMS];
    private final SplitPacketList splitPacketList = new SplitPacketList();
    private final int[] heapIndexOffsets = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] orderedReadIndex = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] sequencedReadIndex = new int[NUMBER_OF_ORDERED_STREAMS];

    private final Set<Integer> hasReceived = new HashSet<>();
    private boolean needBandAs;
    private int receivedCount = 0;
    private long lastArrived;

    public ReliabilityInboundHandler(RakChannel channel) {
        this.channel = channel;

        for (int i = 0; i < NUMBER_OF_ORDERED_STREAMS; i++) {
            //orderingHeaps[i] = new PriorityQueue<>(Comparator.comparingInt(a -> a.weight));
            orderingHeaps[i] = new PriorityQueue<>();
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        lastArrived = System.nanoTime();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // ReliabilityLayer::HandleSocketReceiveFromConnectedPlayer

        if (out == null) {
            out = (ReliabilityOutboundHandler) ctx.pipeline().get(ReliabilityOutboundHandler.NAME);
        }

        long timeRead = System.nanoTime();
        lastArrived = timeRead;

        try {
            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf buf = packet.content();

            DatagramHeader header = new DatagramHeader();
            header.decode(buf);

            // happens if first byte of packet is less than 0x80
            if (!header.isValid) {
                return;
            }

            // if received an ACK packet
            if (header.isAck) {
                AcknowledgePacket ack = new AcknowledgePacket();
                ack.header = header;
                ack.headerLength = buf.readerIndex();
                ack.decode(buf);
                out.onAck(ack, timeRead);
                receivedCount++;
                return;
            }

            // if received an NAK packet
            if (header.isNak) {
                AcknowledgePacket nak = new AcknowledgePacket();
                nak.header = header;
                nak.headerLength = buf.readerIndex();
                nak.decode(buf);
                out.onNak(nak, timeRead);
                receivedCount++;
                return;
            }

            // if it is general connected packet
            {
                int skippedDatagram = channel.slidingWindow().onGotPacket(header.datagramNumber);

                // Note: packet pair is no longer used
                // if (header.isPacketPair)

                // insert missing indices to NAK list
                for (int i = skippedDatagram; skippedDatagram > 0; skippedDatagram--) {
                    out.sendNak(header.datagramNumber - skippedDatagram);
                }

                // flag if the remote system asked for B and As
                needBandAs = header.needsBandAs;

                // insert received indices to ACK list
                out.sendAck(header.datagramNumber);

                while (buf.isReadable()) {
                    InternalPacket internalPacket = new InternalPacket();
                    internalPacket.header = header;
                    //internalPacket.headerLength = buf.readerIndex();
                    internalPacket.decode(buf);
                    readPacket(ctx, internalPacket);
                    receivedCount++;
                }
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void readPacket(ChannelHandlerContext ctx, InternalPacket packet) {
        LOGGER.debug("Packet: " + packet);

        // check if we missed some reliable messages?
        if (packet.reliability.isReliable()) {
            int holeIndex = packet.reliableIndex - receivedBaseIndex;
            int holeSize = receivedTopIndex - receivedBaseIndex + 1;

            if (holeIndex == 0) {
                // got a packet that we were expecting
                // Reliability.cpp#L956 hasReceivedPacketQueue.Pop();
                hasReceived.remove(packet.reliableIndex);
                // move the base index
                ++receivedBaseIndex;
                // fill the hole
                --holeSize;

            } else if (holeIndex < 0) {
                // got a duplicate packet we already got
                ReferenceCountUtil.release(packet);
                return;

            } else if (holeIndex < holeSize) {
                // got a packet that was missing in the sequence
                boolean received = hasReceived.contains(holeIndex);

                if (!received) {
                    // this is a hole, let's fill it
                    hasReceived.add(holeIndex);
                } else {
                    // duplicate packet
                    ReferenceCountUtil.release(packet);
                    return;
                }

            } else {
                // holeIndex >= holeSize
                Validate.isTrue(holeIndex < 100000, "hole count too high");
                // expand the hole
                receivedTopIndex = packet.reliableIndex;
                hasReceived.add(receivedTopIndex);
            }

            while (holeSize > 0) {
                boolean received = hasReceived.contains(receivedBaseIndex);
                if (received) {
                    hasReceived.remove(receivedBaseIndex);
                    ++receivedBaseIndex;
                    --holeSize;
                }
            }

            // keep top always >= base
            receivedTopIndex = Math.max(receivedBaseIndex, receivedTopIndex);
        }

        // reassemble if this is a split packet
        if (packet.splitPacketCount > 0) {

            if (!packet.reliability.isOrdered()) {
                packet.orderingChannel = 255;
            }

            splitPacketList.insert(packet);
            packet = splitPacketList.build(packet.splitPacketId);

            if (packet == null) {
                // we don't have every pieces yet
                return;
            }
        }

        // ordering packet
        if (packet.reliability.isOrdered()) {
            int orderingChannel = packet.orderingChannel;

            if (packet.orderingIndex == orderedReadIndex[orderingChannel]) {
                // got the ordered packet we are expecting

                if (packet.reliability.isSequenced()) {
                    // got a sequenced packet
                    if (!isOlderPacket(packet.sequencingIndex, sequencedReadIndex[orderingChannel])) {
                        // expected or higher sequence value
                        sequencedReadIndex[orderingChannel] = packet.sequencingIndex + 1;

                    } else {
                        // discard: lower sequence value
                        ReferenceCountUtil.release(packet);
                        return;
                    }

                } else {
                    // got a ordered but NOT sequenced packet, with the expecting order
                    ctx.fireChannelRead(packet.data);

                    // increment by 1 for every ordered message sent
                    orderedReadIndex[orderingChannel]++;
                    // reset to 0 when every ordered message sent
                    sequencedReadIndex[orderingChannel] = 0;

                    // check the ordering heap and write those ready to next handler
                    PriorityQueue<HeapedPacket> heap = orderingHeaps[packet.orderingChannel];
                    while (heap.size() > 0) {
                        if (heap.peek().packet.orderingIndex != orderedReadIndex[orderingChannel]) {
                            break;
                        }

                        packet = heap.poll().packet;
                        ctx.fireChannelRead(packet.data);

                        if (packet.reliability == PacketReliability.RELIABLE_ORDERED) {
                            orderedReadIndex[packet.orderingChannel]++;

                        } else {
                            sequencedReadIndex[packet.orderingChannel] = packet.sequencingIndex;
                        }
                    }
                }

                return;
            } else if (!isOlderPacket(packet.orderingIndex, orderedReadIndex[orderingChannel])) {
                // orderingIndex is greater

                // If a message has a greater ordering index, and is sequenced or ordered, buffer it
                // Sequenced has a lower heap weight, ordered has max sequenced weight

                PriorityQueue<HeapedPacket> heap = orderingHeaps[packet.orderingChannel];
                if (heap.size() == 0) {
                    heapIndexOffsets[packet.orderingChannel] = orderedReadIndex[orderingChannel];
                }

                int orderedHoleCount = packet.orderingIndex - heapIndexOffsets[packet.orderingChannel];
                int weight = orderedHoleCount * 0x100000;

                if (packet.reliability.isSequenced()) {
                    weight += packet.sequencingIndex;
                } else {
                    weight += (0x100000 - 1);
                }

                heap.offer(new HeapedPacket(weight, packet));

                // buffered, return
                return;

            } else {
                // out of order packet
                ReferenceCountUtil.release(packet);
                return;
            }
        }

        // Nothing special about this packet. Move it to the next handler
        ctx.fireChannelRead(packet.data);
    }

    private boolean isOlderPacket(int actualIndex, int expectingIndex) {
        // TODO: check this with ReliabilityLayer.cpp#L2901
        return actualIndex < expectingIndex;
    }

    public long lastArrived() {
        return lastArrived;
    }
}