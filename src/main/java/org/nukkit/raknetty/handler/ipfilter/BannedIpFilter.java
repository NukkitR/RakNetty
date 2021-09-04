package org.nukkit.raknetty.handler.ipfilter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.apache.commons.lang3.Validate;
import org.nukkit.raknetty.channel.AddressedOfflineMessage;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.offline.ConnectionBanned;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BannedIpFilter extends ChannelInboundHandlerAdapter {

    private static final InternalLogger LOGGER = InternalLoggerFactory.getInstance(BannedIpFilter.class);

    private final Map<InetAddress, Long> banned = new ConcurrentHashMap<>();
    private final RakServerChannel channel;

    public BannedIpFilter(RakServerChannel channel) {
        this.channel = channel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        boolean release = true;

        try {
            if (msg instanceof DatagramPacket) {
                InetSocketAddress sender = ((DatagramPacket) msg).sender();

                if (isBanned(sender)) {
                    LOGGER.debug("discarding a datagram packet from {}: ip address is banned", sender);

                    ConnectionBanned out = new ConnectionBanned();
                    out.senderGuid = channel.localGuid();
                    ctx.writeAndFlush(new AddressedOfflineMessage(out, sender));
                    return;
                }

                // if the address is not banned, proceed to the next handler
                release = false;
                ctx.fireChannelRead(msg);
            }
        } finally {
            if (release) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    public boolean isBanned(InetSocketAddress remoteAddress) {

        InetAddress address = remoteAddress.getAddress();
        Long expiredTime = banned.get(address);

        if (expiredTime != null) {
            long currentTime = System.nanoTime();

            if (expiredTime > 0 && currentTime - expiredTime > 0) {
                banned.remove(address);
            } else {
                // not expired or a permanent ban
                return true;
            }
        }

        return false;
    }

    /**
     * Bans an IP from connecting. Banned IPs persist between connections.
     *
     * @param remoteAddress address to be banned
     * @param milliseconds  milliseconds for a temporary ban. Use 0 for permanent ban
     */
    public void add(InetSocketAddress remoteAddress, long milliseconds) {

        Validate.notNull(remoteAddress, "The address to be banned is null");
        Validate.isTrue(milliseconds >= 0, "A negative time is not allowed");

        if (milliseconds == 0) {
            banned.put(remoteAddress.getAddress(), 0L);
        } else {
            long time = System.nanoTime();
            banned.put(remoteAddress.getAddress(), time + TimeUnit.NANOSECONDS.convert(milliseconds, TimeUnit.MILLISECONDS));
        }
    }

    public void remove(InetSocketAddress remoteAddress) {

        Validate.notNull(remoteAddress, "The address to be unbanned is null");
        banned.remove(remoteAddress.getAddress());
    }

    public void clear() {
        banned.clear();
    }

}
