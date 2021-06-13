package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.handler.codec.DatagramHeader;
import org.nukkit.raknetty.handler.codec.MTUSize;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.util.PacketUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.nukkit.raknetty.handler.codec.Message.UDP_HEADER_SIZE;
import static org.nukkit.raknetty.handler.codec.reliability.InternalPacket.NUMBER_OF_ORDERED_STREAMS;

public class ReliabilityHandler extends ChannelDuplexHandler {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReliabilityHandler.class);

    private boolean needBandAs = false;
    private final SplitPacketList splitPacketList = new SplitPacketList();
    private final RakChannel channel;

    public ReliabilityHandler(RakChannel channel) {
        for (int i = 0; i < NUMBER_OF_ORDERED_STREAMS; i++) {
            orderingHeaps[i] = new PriorityQueue<>(Comparator.comparingInt(a -> a.weight));
        }

        this.channel = channel;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        this.lastArrived = System.nanoTime();
        this.lastReliableSend = System.nanoTime();
    }

    private SlidingWindow slidingWindow() {
        return channel.slidingWindow();
    }

    public boolean needBandAs() {
        return needBandAs;
    }


    /***
     ========================
     Channel Inbound
     ========================
     */

    /**
     * The packet number we are expecting
     */
    private int receivedBaseIndex = 0;

    /**
     * The maximum packet number we have ever received
     */
    private int receivedTopIndex = 0;

    private final PriorityQueue<HeapedPacket>[] orderingHeaps = new PriorityQueue[NUMBER_OF_ORDERED_STREAMS];
    private final int[] heapIndexOffsets = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] orderedReadIndex = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] sequencedReadIndex = new int[NUMBER_OF_ORDERED_STREAMS];

    private final Set<Integer> hasReceived = new HashSet<>();
    private int receivedCount = 0;
    private long lastArrived;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // ReliabilityLayer::HandleSocketReceiveFromConnectedPlayer

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
                readAck(ack, timeRead);
                receivedCount++;
                return;
            }

            // if received an NAK packet
            if (header.isNak) {
                AcknowledgePacket nak = new AcknowledgePacket();
                nak.header = header;
                nak.headerLength = buf.readerIndex();
                nak.decode(buf);
                readNak(nak, timeRead);
                receivedCount++;
                return;
            }

            // if it is general connected packet
            int skippedMessageCount = slidingWindow().onGotPacket(header.datagramNumber);

            // Note: packet pair is no longer used
            // if (header.isPacketPair)

            // insert missing indices to NAK list
            for (int i = skippedMessageCount; skippedMessageCount > 0; skippedMessageCount--) {
                NAKs.add(header.datagramNumber - skippedMessageCount);
            }

            // flag if the remote system asked for B and As
            needBandAs = header.needsBandAs;

            // insert received indices to ACK list
            ACKs.add(header.datagramNumber);

            InternalPacket internalPacket = new InternalPacket(); //TODO: make it pooled
            internalPacket.header = header;
            internalPacket.headerLength = buf.readerIndex();
            internalPacket.decode(buf);
            readPacket(ctx, internalPacket);
            receivedCount++;

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    public void readAck(AcknowledgePacket ack, long timeRead) {
        LOGGER.debug("ACK: " + ack);
        ack.indices().forEach(messageNumber -> {

            // remove if the message is sent in an unreliable way
            UnreliableAckReceipt receipt = unreliableReceipts.get(messageNumber);
            if (receipt != null) {
                channel.pipeline().fireUserEventTriggered(
                        new AcknowledgeEvent(AcknowledgeEvent.AcknowledgeState.RECEIPT_ACKED, receipt.serial));
                unreliableReceipts.remove(messageNumber);
                return;
            }

            Integer timeSent = datagramHistory.get(messageNumber);
            if (timeSent != null) {
                long ping = timeRead > timeSent ? timeRead - timeSent : 0;
                slidingWindow().onAck(ping, ack);

                //TODO: check if we should implement messageNumberNode->next;
                removeFromResendList(messageNumber);
                // RemoveFromDatagramHistory
                datagramHistory.remove(messageNumber);
            }

            // TODO:
            //  fire event: ack receipt
            //  get message node by index
            //  congestionManager.onAck()
            //  remove index from history
        });
    }

    public void readNak(AcknowledgePacket nak, long timeRead) {
        LOGGER.debug("NAK: " + nak);
        nak.indices().forEach(index -> {
            // ReliabilityLayer.cpp#L831
            slidingWindow().onNak();
            // TODO: check if we should implement messageNumberNode->next;
            //  congestionManager.onNak()
            //  resend immediately

        });
    }

    public void readPacket(ChannelHandlerContext ctx, InternalPacket packet) {
        LOGGER.debug("Packet: " + packet);

        // TODO: implement resetReceivedPackets ReliabilityLayer.cpp#L910

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


    /***
     *
     * ========================
     * Channel Outbound
     * ========================
     *
     */
    private final AcknowledgePacket NAKs = new AcknowledgePacket();
    private final AcknowledgePacket ACKs = new AcknowledgePacket();
    private static final int RESEND_BUFFER_ARRAY_MASK = 511;
    private static final int RESEND_BUFFER_ARRAY_LENGTH = 512;

    private long lastReliableSend;
    private int unackedBytes = 0;
    private boolean bandwidthExceeded = false;

    private int sendReliableIndex = 0;

    private final PriorityQueue<HeapedPacket> sendBuffer = new PriorityQueue<>();
    private final LinkedList<InternalPacket> resendList = new LinkedList<>();
    private long lastUpdated = System.nanoTime();
    private long nextUnreliableCull;

    private final Map<Integer, UnreliableAckReceipt> unreliableReceipts = new HashMap<>();
    private final LinkedList<InternalPacket> unreliableList = new LinkedList<>();

    private final Map<Integer, Integer> datagramHistory = new HashMap<>();
    private final InternalPacket[] resendBuffer = new InternalPacket[RESEND_BUFFER_ARRAY_LENGTH];

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);

        // TODO: congestion control here. store packets before write it out.
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        // TODO: write the number of packets out and flush

        super.flush(ctx);
    }

    private void sendAck() {
        int maxSize = slidingWindow().getMtuExcludingMessageHeader();
        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.ACK);

        while (!ACKs.isEmpty()) {
            // if (needsBandAs) is deprecated

            ByteBuf buf = channel.alloc().ioBuffer();
            header.encode(buf);
            ACKs.encode(buf, maxSize, true);

            channel.udpChannel().write(new DatagramPacket(buf, channel.remoteAddress()));
            slidingWindow().onSendAck();
        }
    }

    private void sendNak() {
        int maxSize = slidingWindow().getMtuExcludingMessageHeader();
        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.NAK);

        ByteBuf buf = channel.alloc().ioBuffer();
        header.encode(buf);
        NAKs.encode(buf, maxSize, true);

        channel.udpChannel().write(new DatagramPacket(buf, channel.remoteAddress()));
    }


    public boolean isAckTimeout(long currentTime) {
        return lastArrived() > 0 && currentTime - (lastArrived() + channel.config().getTimeout()) >= 0;
    }

    public boolean isAckWaiting() {
        return !ACKs.isEmpty();
    }

    public boolean isOutboundDataWaiting() {
        return !sendBuffer.isEmpty();
    }


    private void removeFromResendList(int messageNumber) {
        int arrayIndex = messageNumber & RESEND_BUFFER_ARRAY_MASK;
        InternalPacket packet = resendBuffer[arrayIndex];
        if (packet != null) {
            Validate.isTrue(packet.reliableIndex == messageNumber, "Message number mismatch");

            resendBuffer[arrayIndex] = null;
            LOGGER.debug("AckRcv: {}", messageNumber);

            // TODO: statistic staff

            // if receipt is asked or it is the last piece of split packets
            if (packet.reliability.withAckReceipt() &&
                    (packet.splitPacketCount == 0 || packet.splitPacketIndex + 1 == packet.splitPacketCount)) {
                channel.pipeline().fireUserEventTriggered(
                        new AcknowledgeEvent(AcknowledgeEvent.AcknowledgeState.RECEIPT_ACKED, packet.receiptSerial));
            }

            // TODO: unacknowledgedBytes?
            resendList.remove(packet);
        }
    }

    public long lastReliableSend() {
        return lastReliableSend;
    }

    public void writeDatagram(DatagramHeader header, List<InternalPacket> packets) {
        ByteBuf buf = channel.alloc().ioBuffer();

    }

    /***
     *
     * ========================
     * Updating
     * ========================
     *
     */
    public void update() {

        long currentTime = System.nanoTime();

        // update time
        if (currentTime <= lastUpdated) {
            lastUpdated = currentTime;
            return;
        }

        long elapsedTime = Math.min(currentTime - lastUpdated, TimeUnit.MILLISECONDS.toNanos(100));
        lastUpdated = currentTime;

        // check unreliable timeout
        int unreliableTimeout = channel.config().getUnreliableTimeout();
        if (unreliableTimeout > 0) {
            if (elapsedTime - nextUnreliableCull >= 0) {

                Iterator<InternalPacket> iterator = unreliableList.iterator();
                while (true) {
                    InternalPacket packet = iterator.next();
                    if (currentTime - (packet.creationTime + unreliableTimeout) >= 0) {
                        iterator.remove();
                    } else {
                        break;
                    }
                }

                nextUnreliableCull = unreliableTimeout / 2;
            } else {
                nextUnreliableCull -= elapsedTime;
            }
        }

        // send ACKs
        if (slidingWindow().shouldSendAck(currentTime)) {
            // ReliabilityLayer.cpp#L1813
            sendAck();
        }

        if (!NAKs.isEmpty()) {
            sendNak();
        }


        Iterator<Map.Entry<Integer, UnreliableAckReceipt>> it = unreliableReceipts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, UnreliableAckReceipt> history = it.next();
            //int messageNum = history.getKey();
            UnreliableAckReceipt receipt = history.getValue();

            if (receipt.actionTime - currentTime >= 0) {
                channel.pipeline().fireUserEventTriggered(
                        new AcknowledgeEvent(AcknowledgeEvent.AcknowledgeState.RECEIPT_LOSS, receipt.serial));

                it.remove();
            }
        }

        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.DATA);
        boolean needsBandAs = slidingWindow().isSlowStart();
        header.isContinuousSend = this.bandwidthExceeded;
        header.needsBandAs = needsBandAs;
        this.bandwidthExceeded = !sendBuffer.isEmpty();

        // if we have data to send or resend
        if (!bandwidthExceeded || !resendList.isEmpty()) {

            InternalPacket packet;
            boolean pushed = false;

            int transmissionBandwidth = slidingWindow().getTransmissionBandwidth(unackedBytes, bandwidthExceeded);
            int retransmissionBandwidth = slidingWindow().getRetransmissionBandwidth(unackedBytes);
            int datagramSizes = 0; // size of datagram so far
            int datagramNum = 0; // number of datagram to be sent

            if (transmissionBandwidth > 0 || retransmissionBandwidth > 0) {

                List<InternalPacket> sendList = new ArrayList<>(); // list of packets to be send

                // keep filling datagrams until we exceed the retransmission bandwidth
                while (datagramSizes < retransmissionBandwidth) {
                    Iterator<InternalPacket> iterator = resendList.iterator();
                    while (iterator.hasNext()) {
                        packet = iterator.next();
                        Validate.isTrue(packet.header != null);
                        Validate.isTrue(packet.header.datagramNumber >= 0);

                        if (packet.actionTime - currentTime >= 0) {
                            int packetLength = packet.headerLength + packet.bodyLength;
                            if (datagramSizes + packetLength > slidingWindow().getMtuExcludingMessageHeader()) {
                                // pushDatagram();
                                writeDatagram(header, sendList);
                                header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                                datagramNum++;
                                break;
                            }

                            // remove the packet from the resend list head
                            iterator.remove();
                            LOGGER.debug("Resending reliable #{}", packet.reliableIndex);

                            // push packet, see ReliabilityLayer::PushPacket
                            datagramSizes += packetLength;
                            Validate.isTrue(datagramSizes < MTUSize.MAXIMUM_MTU_SIZE - UDP_HEADER_SIZE);
                            sendList.add(packet);
                            packet.timeSent++;

                            slidingWindow().onResend();
                            packet.retransmissionTime = slidingWindow().getRtoForRetransmission();
                            packet.actionTime = currentTime + packet.retransmissionTime;

                            pushed = true;

                            // add the packet back into the resend list
                            resendList.add(packet);
                        } else {
                            // push packets into a datagram
                            writeDatagram(header, sendList);
                            header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                            datagramNum++;
                            sendList.clear();
                            break;
                        }
                    }

                    if (!pushed) break;
                }
            }

            if (datagramSizes < transmissionBandwidth) {

                List<InternalPacket> sendList = new ArrayList<>();

                datagramSizes = 0;

                // check if the resend buffer is overflowed?
                while (datagramSizes < transmissionBandwidth) {
                    pushed = false;

                    // make sure the resend buffer is not overflowed
                    if (resendBuffer[sendReliableIndex & RESEND_BUFFER_ARRAY_MASK] != null) {
                        break;
                    }

                    while (!sendBuffer.isEmpty()) {
                        // make sure the resend buffer is not overflowed
                        if (resendBuffer[sendReliableIndex & RESEND_BUFFER_ARRAY_MASK] != null) {
                            break;
                        }

                        packet = sendBuffer.peek().packet;
                        Validate.isTrue(packet.header.datagramNumber < 0); // is message number not assigned?
                        //Validate.isTrue(packet.bodyLength < MTUSize.MAXIMUM_MTU_SIZE);

                        if (packet.data == null) {
                            sendBuffer.poll();
                            continue;
                        }

                        packet.headerLength = PacketUtil.getHeaderLength(packet);
                        int packetLength = packet.headerLength + packet.bodyLength;

                        if (datagramSizes + packetLength > slidingWindow().getMtuExcludingMessageHeader()) {
                            // hit MTU, stop pushing packets
                            break;
                        }

                        if (packet.reliability.isReliable()) {
                            packet.header.datagramNumber = sendReliableIndex;
                            packet.retransmissionTime = slidingWindow().getRtoForRetransmission();
                            packet.actionTime = currentTime + packet.retransmissionTime;

                            // push packet into resend buffer
                            resendBuffer[packet.reliableIndex & RESEND_BUFFER_ARRAY_MASK] = packet;

                            // insert packet into resend list
                            resendList.add(packet);
                            unackedBytes += packetLength;

                            sendReliableIndex++;

                        } else if (packet.reliability == PacketReliability.UNRELIABLE_WITH_ACK_RECEIPT) {
                            int messageNumber = slidingWindow().getNextDatagramNumber() + datagramNum;
                            int receiptSerial = packet.receiptSerial;
                            long actionTime = slidingWindow().getRtoForRetransmission() + currentTime;

                            unreliableReceipts.put(messageNumber, new UnreliableAckReceipt(receiptSerial, actionTime));
                        }

                        // push packet, see ReliabilityLayer::PushPacket
                        datagramSizes += packetLength;
                        Validate.isTrue(datagramSizes < MTUSize.MAXIMUM_MTU_SIZE - UDP_HEADER_SIZE);
                        sendList.add(packet);
                        packet.timeSent++;

                        pushed = true;
                    }

                    if (!pushed) break;

                    // push packets into a datagram
                    writeDatagram(header, sendList);
                    header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                    datagramNum++;
                    sendList.clear();
                }
            }

            bandwidthExceeded = !sendBuffer.isEmpty();
        }
    }


    private static final class HeapedPacket {
        int weight;
        InternalPacket packet;

        public HeapedPacket(int weight, InternalPacket packet) {
            this.weight = weight;
            this.packet = packet;
        }
    }

    private static final class UnreliableAckReceipt {
        public int serial;
        public long actionTime;

        public UnreliableAckReceipt(int receiptSerial, long actionTime) {
            this.serial = receiptSerial;
            this.actionTime = actionTime;
        }
    }
}
