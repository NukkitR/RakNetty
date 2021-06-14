package org.nukkit.raknetty.handler.codec.reliability;

import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.handler.codec.DatagramHeader;
import org.nukkit.raknetty.handler.codec.MTUSize;

import java.util.concurrent.TimeUnit;

public class SlidingWindow {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(SlidingWindow.class);


    public static final int UDP_HEADER_SIZE = 28;
    public static final long SYN = TimeUnit.MICROSECONDS.toNanos(10000);
    public static final double UNSET_TIME_NS = -1.0d;

    private int mtuSize;
    private double cwnd;
    private double ssThresh = 0.0;

    private double lastRtt = UNSET_TIME_NS;
    private double estimatedRtt = UNSET_TIME_NS;
    private double deviationRtt = UNSET_TIME_NS;

    private long oldestUnsentAck = 0L;

    private int nextDatagramNumber = 0;
    private int expectedDatagramNumber = 0;
    private int nextCongestionControlBlock = 0;

    private boolean backoff = false;
    private boolean speedup = false;
    private boolean isContinuousSend = false;

    public SlidingWindow(int mtuSize) {
        this.setMtu(mtuSize);
        cwnd = this.mtuSize;
    }

    public int getRetransmissionBandwidth(int unackedBytes) {
        return unackedBytes;
    }

    public int getTransmissionBandwidth(int unackedBytes, boolean isContinuousSend) {

        this.isContinuousSend = isContinuousSend;

        if (unackedBytes <= cwnd) {
            return (int) (cwnd - unackedBytes);
        }

        return 0;
    }

    public boolean shouldSendAck(long currentTime) {
        long rto = getSenderRtoForAck();

        if (rto == (long) UNSET_TIME_NS) {
            return true;
        }

        return currentTime - (oldestUnsentAck + SYN) >= 0;
    }

    public int getNextDatagramNumber() {
        return this.nextDatagramNumber;
    }

    public int increaseDatagramNumber() {
        return ++nextDatagramNumber;
    }

    /**
     * @param datagramNumber Packet datagram number
     * @return number of messages skipped
     */
    public int onGotPacket(int datagramNumber) {
        if (oldestUnsentAck == 0) {
            oldestUnsentAck = System.nanoTime(); //TODO: check CCRakNetSlidingWindow.cpp#L135
        }

        if (datagramNumber == expectedDatagramNumber) {
            expectedDatagramNumber = datagramNumber + 1;
            return 0;

        } else if (datagramNumber > expectedDatagramNumber) {//TODO: check CCRakNetSlidingWindow.cpp#L142
            int skipped = datagramNumber - expectedDatagramNumber;
            if (skipped > 1000) skipped = 1000;
            expectedDatagramNumber = datagramNumber + 1;
            return skipped;

        }

        return 0;
    }

    public void onResend() {
        if (isContinuousSend && !backoff && cwnd > mtuSize * 2) {
            ssThresh = cwnd / 2;
            if (ssThresh < mtuSize) ssThresh = mtuSize;

            cwnd = mtuSize;

            // only backoff once per period
            nextCongestionControlBlock = nextDatagramNumber;
            backoff = true;

            LOGGER.debug("Enter slow start, cwnd = {}", cwnd);
        }
    }

    public void onNak() {
        if (isContinuousSend && !backoff) {
            // Start congestion avoidance
            ssThresh = cwnd / 2;

            LOGGER.debug("Set congestion avoidance due to NAK, cwnd = {}", cwnd);
        }
    }

    public void onAck(long rtt, AcknowledgePacket ack) {
        DatagramHeader header = ack.header;
        int datagramNumber = header.datagramNumber;

        lastRtt = rtt;

        if (estimatedRtt == UNSET_TIME_NS) {
            estimatedRtt = rtt;
            deviationRtt = rtt;

        } else {
            double diff = rtt - estimatedRtt;
            estimatedRtt = estimatedRtt + 0.5d * diff;
            deviationRtt = deviationRtt + 0.5d * (Math.abs(diff) - deviationRtt);
        }

        this.isContinuousSend = header.isContinuousSend;
        if (!isContinuousSend) {
            return;
        }

        boolean shouldCongestionControl = datagramNumber > nextCongestionControlBlock;

        if (shouldCongestionControl) {
            backoff = false;
            speedup = false;
            nextCongestionControlBlock = nextDatagramNumber;
        }

        if (isSlowStart()) {
            cwnd += mtuSize;
            if (cwnd > ssThresh && ssThresh != 0) {
                cwnd = ssThresh + mtuSize * mtuSize / cwnd;
            }

            LOGGER.debug("Slow start increase, cwnd = {}", cwnd);

        } else if (shouldCongestionControl) {
            cwnd += mtuSize * mtuSize / cwnd;

            LOGGER.debug("Congestion avoidance increase, cwnd = {}", cwnd);
        }
    }

    public void onSendAck() {
        oldestUnsentAck = 0;
    }

    public long getRtoForRetransmission() {

        long maxThreshold = TimeUnit.MILLISECONDS.toNanos(2000);
        long additionalVariance = TimeUnit.MILLISECONDS.toNanos(30);

        if (estimatedRtt == UNSET_TIME_NS) {
            return maxThreshold;
        }

        double u = 2.0d;
        double q = 4.0d;

        long threshold = (long) ((u * estimatedRtt + q * deviationRtt) + additionalVariance);
        if (threshold > maxThreshold) {
            return maxThreshold;
        }

        return threshold;
    }

    public void setMtu(int mtuSize) {
        Validate.isTrue(mtuSize <= MTUSize.MAXIMUM_MTU_SIZE);
        this.mtuSize = mtuSize;
    }

    public int getMtu() {
        return this.mtuSize;
    }

    public int getMtuExcludingMessageHeader() {
        return this.mtuSize - DatagramHeader.HEADER_LENGTH_BYTES;
    }

    public double getRtt() {
        if (lastRtt == UNSET_TIME_NS) {
            return 0.0d;
        }

        return lastRtt;
    }

    public long getSenderRtoForAck() {
        if (lastRtt == UNSET_TIME_NS) {
            return (long) UNSET_TIME_NS;
        }
        return (long) (lastRtt + SYN);
    }

    public boolean isSlowStart() {
        return cwnd <= ssThresh || ssThresh == 0;
    }

}
