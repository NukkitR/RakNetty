package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.RakChannel;
import org.nukkit.raknetty.channel.event.ReceiptAcknowledgeEvent;
import org.nukkit.raknetty.handler.codec.*;
import org.nukkit.raknetty.util.RakNetUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.nukkit.raknetty.handler.codec.InternalPacket.NUMBER_OF_ORDERED_STREAMS;
import static org.nukkit.raknetty.handler.codec.Message.UDP_HEADER_SIZE;

public class ReliabilityOutboundHandler extends ChannelOutboundHandlerAdapter {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(ReliabilityOutboundHandler.class);
    public static final String NAME = "ReliabilityOut";

    private final RakChannel channel;
    private ChannelHandlerContext ctx;
    private ReliabilityInboundHandler in;

    private final AcknowledgePacket NAKs = new AcknowledgePacket();
    private final AcknowledgePacket ACKs = new AcknowledgePacket();
    private static final int RESEND_BUFFER_ARRAY_MASK = 511;
    private static final int RESEND_BUFFER_ARRAY_LENGTH = 512;

    private long lastReliableSend;
    private int unackedBytes = 0;
    private boolean bandwidthExceeded = false;

    private int sendSplitId = 0;
    private int reliableWriteIndex = 0;
    private final int[] orderedWriteIndex = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] sequencedWriteIndex = new int[NUMBER_OF_ORDERED_STREAMS];
    private final int[] sendWeights = new int[4];

    private final PriorityQueue<HeapedPacket> sendBuffer = new PriorityQueue<>();
    private final LinkedList<InternalPacket> resendList = new LinkedList<>();
    private long lastUpdated = System.nanoTime();
    private long nextUnreliableCull;

    private final Map<Integer, UnreliableAckReceipt> unreliableReceipts = new HashMap<>();
    private final LinkedList<InternalPacket> unreliableList = new LinkedList<>();

    private final DatagramHistory datagramHistory = new DatagramHistory();
    private final InternalPacket[] resendBuffer = new InternalPacket[RESEND_BUFFER_ARRAY_LENGTH];

    public ReliabilityOutboundHandler(RakChannel channel) {
        this.channel = channel;

        for (int i = 0; i < PacketPriority.NUMBER_OF_PRIORITIES.ordinal(); i++) {
            sendWeights[i] = (1 << i) * i + i;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
    }

    public void sendAck(int datagramNumber) {
        ACKs.add(datagramNumber);
        NAKs.remove(datagramNumber); // remove it from the NAK list if previously added
    }

    public void sendNak(int datagramNumber) {
        NAKs.add(datagramNumber);
    }

    public void onAck(AcknowledgePacket ack, long timeRead) {
        // LOGGER.debug("RECV_ACK: " + ack);
        ack.indices().forEach(datagramNumber -> {

            // remove if the message is sent in an unreliable way
            UnreliableAckReceipt receipt = unreliableReceipts.get(datagramNumber);
            if (receipt != null) {
                channel.pipeline().fireUserEventTriggered(
                        new ReceiptAcknowledgeEvent(ReceiptAcknowledgeEvent.AcknowledgeState.ACKED, receipt.serial));
                unreliableReceipts.remove(datagramNumber);
                return;
            }

            DatagramHistory.Node node = datagramHistory.get(datagramNumber);
            if (node != null) {
                long timeSent = node.timeSent;
                long ping = Math.max(timeRead - timeSent, 0);
                channel.slidingWindow().onAck(ping, ack);

                // remove message from resend list and delete older reliable sequenced.
                node.messages.forEach(this::removeFromResendList);

                // remove from datagram history
                datagramHistory.remove(datagramNumber);
            }
        });
    }

    public void onNak(AcknowledgePacket nak, long timeRead) {
        LOGGER.debug("RECV_NAK: " + nak);
        nak.indices().forEach(datagramNumber -> {
            // ReliabilityLayer.cpp#L831
            channel.slidingWindow().onNak();

            DatagramHistory.Node node = datagramHistory.get(datagramNumber);
            if (node != null) {
                // update timers so resends occur in this next update.
                node.messages.forEach(messageNumber -> {
                    InternalPacket packet = resendBuffer[messageNumber & RESEND_BUFFER_ARRAY_MASK];
                    if (packet != null && packet.actionTime != 0) {
                        packet.actionTime = timeRead;
                    }
                });
            }
        });
    }

    protected void doSendAck() {
        int mtuSize = channel.mtuSize();
        int maxSize = channel.slidingWindow().getMtuExcludingMessageHeader();
        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.ACK);

        while (!ACKs.isEmpty()) {
            // if (needsBandAs) is deprecated
            // LOGGER.debug("SEND_ACK: {}", ACKs);

            ByteBuf buf = channel.alloc().ioBuffer(mtuSize, mtuSize);
            header.encode(buf);
            ACKs.encode(buf, maxSize, true);

            // no need to buffer the ACK packet, send it right away
            //channel.pipeline().writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
            ctx.writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
            channel.slidingWindow().onSendAck();
        }
    }

    protected void doSendNak() {
        LOGGER.debug("SEND_NAK: {}", NAKs);

        int mtuSize = channel.mtuSize();
        int maxSize = channel.slidingWindow().getMtuExcludingMessageHeader();
        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.NAK);

        ByteBuf buf = channel.alloc().ioBuffer(mtuSize, mtuSize);
        header.encode(buf);
        NAKs.encode(buf, maxSize, true);

        // no need to buffer the NAK packet, send it right away
        //channel.pipeline().writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
        ctx.writeAndFlush(new DatagramPacket(buf, channel.remoteAddress()));
    }

    public boolean isAckTimeout(long currentTime) {
        if (in == null) {
            in = (ReliabilityInboundHandler) channel.pipeline().get(ReliabilityInboundHandler.NAME);
        }

        return in.lastArrived() > 0 &&
                currentTime - (in.lastArrived() + TimeUnit.MILLISECONDS.toNanos(channel.config().getTimeoutMillis())) >= 0;
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
        if (packet != null && packet.reliableMessageNumber == messageNumber) {
            //Validate.isTrue(packet.reliableMessageNumber == messageNumber, "Reliable message number mismatch, expecting: %d, actual: %d", messageNumber, packet.reliableMessageNumber);
            resendBuffer[arrayIndex] = null;
            // TODO: statistic staff

            // if receipt is asked or it is the last piece of split packets
            if (packet.reliability.withAckReceipt() &&
                    (packet.splitPacketCount == 0 || packet.splitPacketIndex + 1 == packet.splitPacketCount)) {
                channel.pipeline().fireUserEventTriggered(
                        new ReceiptAcknowledgeEvent(ReceiptAcknowledgeEvent.AcknowledgeState.ACKED, packet.receiptSerial));
            }

            if (packet.reliability.isReliable()) {
                Validate.isTrue(unackedBytes > packet.headerLength - packet.bodyLength());
                unackedBytes -= packet.headerLength + packet.bodyLength();
            }
            resendList.remove(packet);

            // release the memory
            ReferenceCountUtil.release(packet);
        } else {
            //LOGGER.debug("Unable to remove #{}", messageNumber);
            if (packet != null) {
                LOGGER.warn("Reliable message number mismatch, expecting: %d, actual: %d", messageNumber, packet.reliableMessageNumber);
            }
        }
    }

    public long lastReliableSend() {
        return lastReliableSend;
    }

    private void writeDatagram(DatagramHeader header, List<InternalPacket> packets) {
        Validate.isTrue(!packets.isEmpty());

        int mtuSize = channel.mtuSize();
        ByteBuf buf = channel.alloc().ioBuffer(mtuSize, mtuSize);

        header.datagramNumber = channel.slidingWindow().increaseDatagramNumber();
        header.encode(buf);

        packets.forEach(packet -> {
            if (packet.reliability.isReliable() || packet.reliability.withAckReceipt()) {
                datagramHistory.add(header.datagramNumber, packet.reliableMessageNumber);
            }

            packet.encode(buf);
        });

        if (datagramHistory.get(header.datagramNumber) == null) {
            // add dummy node for unreliable packets
            datagramHistory.add(header.datagramNumber, 0);
        }

        ctx.write(new DatagramPacket(buf, channel.remoteAddress()));
        //channel.pipeline().write(new DatagramPacket(buf, channel.remoteAddress()));
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        // congestion control, buffer the packets before sending it out.

        long currentTime = System.nanoTime();

        if (msg instanceof InternalPacket) {
            InternalPacket packet = (InternalPacket) msg;
            int maxSize = channel.slidingWindow().getMtuExcludingMessageHeader() - Message.MESSAGE_HEADER_MAX_SIZE;
            boolean splitPacket = packet.bodyLength() > maxSize;

            if (splitPacket) {
                switch (packet.reliability) {
                    case UNRELIABLE:
                        packet.reliability = PacketReliability.RELIABLE;
                        break;
                    case UNRELIABLE_WITH_ACK_RECEIPT:
                        packet.reliability = PacketReliability.RELIABLE_WITH_ACK_RECEIPT;
                        break;
                    case UNRELIABLE_SEQUENCED:
                        packet.reliability = PacketReliability.RELIABLE_SEQUENCED;
                        break;
                    default:
                        break;
                }
            }

            if (packet.reliability.isSequenced()) {
                int orderingChannel = packet.orderingChannel;
                packet.orderingIndex = orderedWriteIndex[orderingChannel];
                packet.sequencingIndex = sequencedWriteIndex[orderingChannel]++;

            } else if (packet.reliability.isOrdered()) {
                int orderingChannel = packet.orderingChannel;
                packet.orderingIndex = orderedWriteIndex[orderingChannel]++;
                sequencedWriteIndex[orderingChannel] = 0;
            }

            if (packet.reliability.isReliable()) {
                lastReliableSend = currentTime;
            }

            if (splitPacket) {
                //LOGGER.debug("SPLIT: body length {} is greater than the maximum size {}", packet.bodyLength(), maxSize);
                doSplitPacket(packet);
                return;
            }

            if (!packet.reliability.isReliable()) {
                unreliableList.add(packet);
            }

            //LOGGER.debug("BUFFERED: {}", packet);

            sendBuffer.add(new HeapedPacket(nextWeight(packet.priority), packet));

        } else {
            ctx.write(msg, promise);
        }
    }

    private void doSplitPacket(InternalPacket packet) {

        Validate.isTrue(channel.mtuSize() > 0, "mtu size is not defined.");

        packet.splitPacketCount = 1; // mark it as split packet by assigning an arbitrary value
        int headerLength = RakNetUtil.getHeaderLength(packet);
        int bodyLength = packet.bodyLength();
        int blockSize = channel.slidingWindow().getMtuExcludingMessageHeader() - Message.MESSAGE_HEADER_MAX_SIZE;

        int splitPacketCount = ((bodyLength - 1) / blockSize + 1);

        InternalPacket[] arr = new InternalPacket[splitPacketCount];

        int splitPacketIndex = 0;

        do {
            int size = Math.min(packet.data.readableBytes(), blockSize);

            InternalPacket p = new InternalPacket();

            // copy chunk of data
            p.data = channel.alloc().ioBuffer(size, size);
            p.data.writeBytes(packet.data, size);

            // copy properties
            p.header = packet.header;
            p.headerLength = headerLength;
            p.reliableMessageNumber = packet.reliableMessageNumber;
            p.sequencingIndex = packet.sequencingIndex;
            p.orderingIndex = packet.orderingIndex;
            p.orderingChannel = packet.orderingChannel;
            p.reliability = packet.reliability;
            p.priority = packet.priority;

            // assign split indices
            p.splitPacketIndex = splitPacketIndex;
            p.splitPacketId = sendSplitId;
            p.splitPacketCount = splitPacketCount;

            arr[splitPacketIndex] = p;

            Validate.isTrue(p.bodyLength() < channel.mtuSize());

            if (!p.reliability.isReliable()) {
                unreliableList.add(p);
            }

            //LOGGER.debug("BUFFERED: Split packet #{}: {}/{}", sendSplitId, splitPacketIndex, splitPacketCount);

            sendBuffer.add(new HeapedPacket(nextWeight(p.priority), p));

        } while (++splitPacketIndex < splitPacketCount);

        sendSplitId++;

        // release the packet before split
        ReferenceCountUtil.release(packet);
    }

    private int nextWeight(PacketPriority priority) {
        int priorityLevel = priority.ordinal();
        int nextWeight = sendWeights[priorityLevel];
        if (!sendBuffer.isEmpty()) {
            HeapedPacket head = sendBuffer.peek();
            int peekPL = head.packet.priority.ordinal();
            int weight = head.weight;
            int min = weight - (1 << peekPL) * peekPL + peekPL;

            if (nextWeight < min) nextWeight = min + (1 << priorityLevel) * priorityLevel + priorityLevel;

            sendWeights[priorityLevel] = nextWeight + (1 << priorityLevel) * priorityLevel + priorityLevel;

        } else {
            for (int i = 0; i < PacketPriority.NUMBER_OF_PRIORITIES.ordinal(); i++) {
                sendWeights[i] = (1 << i) * i + i;
            }
        }
        return nextWeight;
    }

    public void update() {
        long currentTime = System.nanoTime();
        Validate.isTrue(ctx != null);

        // update time
        if (currentTime <= lastUpdated) {
            lastUpdated = currentTime;
            return;
        }

        long elapsedTime = Math.min(currentTime - lastUpdated, TimeUnit.MILLISECONDS.toNanos(100));
        lastUpdated = currentTime;

        if (channel.slidingWindow() == null || channel.mtuSize() <= 0) {
            // sliding window is not created yet, meaning that we don't know the mtu size
            return;
        }

        int mtuSize = channel.mtuSize();

        // check unreliable timeout
        long unreliableTimeout = TimeUnit.MILLISECONDS.toNanos(channel.config().getUnreliableTimeoutMillis());
        if (unreliableTimeout > 0) {
            if (elapsedTime - nextUnreliableCull >= 0) {

                Iterator<InternalPacket> iterator = unreliableList.iterator();
                while (true) {
                    InternalPacket packet = iterator.next();
                    if (currentTime - (packet.creationTime + unreliableTimeout) >= 0) {
                        // unreliable timed out
                        iterator.remove();
                        // release
                        ReferenceCountUtil.release(packet);
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
        if (channel.slidingWindow().shouldSendAck(currentTime)) {
            // ReliabilityLayer.cpp#L1813
            doSendAck();
        }

        if (!NAKs.isEmpty()) {
            doSendNak();
        }


        Iterator<Map.Entry<Integer, UnreliableAckReceipt>> it = unreliableReceipts.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, UnreliableAckReceipt> history = it.next();
            UnreliableAckReceipt receipt = history.getValue();

            if (currentTime - receipt.actionTime >= 0) {
                channel.pipeline().fireUserEventTriggered(
                        new ReceiptAcknowledgeEvent(ReceiptAcknowledgeEvent.AcknowledgeState.LOSS, receipt.serial));
                it.remove();
            }
        }

        DatagramHeader header = DatagramHeader.getHeader(DatagramHeader.Type.DATA);
        boolean needsBandAs = channel.slidingWindow().isSlowStart();
        header.isContinuousSend = this.bandwidthExceeded;
        header.needsBandAs = needsBandAs;
        this.bandwidthExceeded = !sendBuffer.isEmpty();

        // if we have data to send or resend
        if (bandwidthExceeded || !resendList.isEmpty()) {

            InternalPacket packet;
            boolean pushed = false;

            int transmissionBandwidth = channel.slidingWindow().getTransmissionBandwidth(unackedBytes, bandwidthExceeded);
            int retransmissionBandwidth = channel.slidingWindow().getRetransmissionBandwidth(unackedBytes);
            int datagramSizes = 0; // size of datagram so far
            int totalSize = 0; // size of all datagram so far
            int datagramNum = 0; // number of datagram to be sent

            if (transmissionBandwidth > 0 || retransmissionBandwidth > 0) {

                List<InternalPacket> sendList = new ArrayList<>(); // list of packets to be send
                List<InternalPacket> addToResendList = new LinkedList<>(); // list of packets to be resend

                // keep filling datagrams until we exceed the retransmission bandwidth
                while (totalSize < retransmissionBandwidth) {
                    Iterator<InternalPacket> iterator = resendList.iterator();
                    while (iterator.hasNext()) {
                        packet = iterator.next();

                        Validate.isTrue(packet.reliableMessageNumber >= 0);

                        if (currentTime - packet.actionTime >= 0) {
                            int packetLength = packet.headerLength + packet.bodyLength();
                            if (datagramSizes + packetLength > channel.slidingWindow().getMtuExcludingMessageHeader()) {
                                // hit the MTU, push datagram
                                Validate.isTrue(datagramSizes > 0);
                                Validate.isTrue(packetLength < mtuSize);

                                writeDatagram(header, sendList);
                                header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                                datagramNum++;
                                datagramSizes = 0;
                                sendList.clear();
                                break;
                            }

                            // remove the packet from the resend list head
                            iterator.remove();

                            // push packet, see ReliabilityLayer::PushPacket
                            datagramSizes += packetLength;
                            totalSize += packetLength;
                            Validate.isTrue(datagramSizes < mtuSize - UDP_HEADER_SIZE);
                            sendList.add(packet);
                            packet.timeSent++;

                            channel.slidingWindow().onResend();
                            packet.retransmissionTime = channel.slidingWindow().getRtoForRetransmission();
                            packet.actionTime = currentTime + packet.retransmissionTime;

                            LOGGER.debug("Resending reliable #{} (rto: {}ms)", packet.reliableMessageNumber, TimeUnit.NANOSECONDS.toMillis(packet.retransmissionTime));

                            pushed = true;

                            // add the packet back into the resend list
                            addToResendList.add(packet);
                        } else {
                            // the remaining packets require no action, push packets into a datagram
                            break;
                        }
                    }

                    // if we have remaining packets, push into a datagram
                    if (datagramSizes > 0) {
                        writeDatagram(header, sendList);
                        header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                        datagramNum++;
                        datagramSizes = 0;
                        sendList.clear();
                        break;
                    }

                    if (!pushed) break;
                }

                // packets which have been marked to be re-sent, add them back into the list
                resendList.addAll(addToResendList);
                addToResendList.clear();
            }

            if (totalSize < transmissionBandwidth) {

                List<InternalPacket> sendList = new ArrayList<>();
                totalSize = 0;

                // check if the resend buffer is overflowed?
                while (!isResendBufferOverflow() && totalSize < transmissionBandwidth) {
                    pushed = false;

                    while (!sendBuffer.isEmpty()) {

                        packet = sendBuffer.peek().packet;
                        Validate.isTrue(packet.header == null); // is message number not assigned?
                        Validate.isTrue(packet.reliableMessageNumber < 0); // is message number not assigned?
                        //Validate.isTrue(packet.bodyLength < MTUSize.MAXIMUM_MTU_SIZE);

                        if (packet.data == null) {
                            sendBuffer.poll();
                            continue;
                        }

                        packet.headerLength = RakNetUtil.getHeaderLength(packet);
                        int packetLength = packet.headerLength + packet.bodyLength();

                        if (datagramSizes + packetLength > channel.slidingWindow().getMtuExcludingMessageHeader()) {
                            // hit MTU, stop pushing packets
                            Validate.isTrue(datagramSizes > 0);
                            Validate.isTrue(packetLength < mtuSize);
                            break;
                        }

                        sendBuffer.poll();

                        if (packet.reliability.isReliable()) {
                            // make sure the resend buffer is not overflowed
                            Validate.isTrue(!isResendBufferOverflow(), "resend buffer is overflowed");

                            packet.reliableMessageNumber = reliableWriteIndex;
                            packet.retransmissionTime = channel.slidingWindow().getRtoForRetransmission();
                            packet.actionTime = currentTime + packet.retransmissionTime;

                            // push packet into resend buffer
                            resendBuffer[packet.reliableMessageNumber & RESEND_BUFFER_ARRAY_MASK] = packet;

                            // insert packet into resend list
                            resendList.add(packet);
                            unackedBytes += packetLength;

                            reliableWriteIndex++;

                        } else if (packet.reliability == PacketReliability.UNRELIABLE_WITH_ACK_RECEIPT) {
                            int messageNumber = channel.slidingWindow().getNextDatagramNumber() + datagramNum;
                            int receiptSerial = packet.receiptSerial;
                            long actionTime = channel.slidingWindow().getRtoForRetransmission() + currentTime;

                            unreliableReceipts.put(messageNumber, new UnreliableAckReceipt(receiptSerial, actionTime));
                        }

                        // push packet, see ReliabilityLayer::PushPacket
                        datagramSizes += packetLength;
                        totalSize += packetLength;
                        Validate.isTrue(datagramSizes < mtuSize - UDP_HEADER_SIZE);
                        sendList.add(packet);
                        packet.timeSent++;

                        pushed = true;

                        if (isResendBufferOverflow()) break;
                    }

                    if (!pushed) break;

                    // push packets into a datagram
                    writeDatagram(header, sendList);
                    header.isContinuousSend = true; // set isContinuousSend to true for subsequent datagrams
                    datagramNum++;
                    datagramSizes = 0;
                    sendList.clear();
                }
            }

            bandwidthExceeded = !sendBuffer.isEmpty();

            // flush the channel after each update to let the datagram going through the pipeline
            if (datagramNum > 0) {
                //channel.pipeline().flush();
                ctx.flush();
            }
        }
    }

    private boolean isResendBufferOverflow() {
        return resendBuffer[reliableWriteIndex & RESEND_BUFFER_ARRAY_MASK] != null;
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
