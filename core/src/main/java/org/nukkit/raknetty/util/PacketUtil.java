package org.nukkit.raknetty.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.CharsetUtil;
import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.PacketReliability;
import org.nukkit.raknetty.handler.codec.reliability.InternalPacket;

import java.net.*;

public class PacketUtil {

    public static byte getByte(ByteBuf buf) {
        return buf.getByte(buf.readerIndex());
    }

    public static MessageIdentifier getMessageIdentifier(ByteBuf buf) {
        return MessageIdentifier.valueOf(getByte(buf));
    }

    public static InetSocketAddress readAddress(ByteBuf buf) {
        byte ipVersion = buf.readByte();
        byte[] binary;
        int port;

        if (ipVersion == 4) {
            binary = new byte[4];

            for (int i = 0; i < 4; i++) {
                binary[i] = (byte) ~buf.readByte();
            }

            port = buf.readUnsignedShort();

        } else if (ipVersion == 6) {
            binary = new byte[16];

            buf.skipBytes(2);   // AF_INET6.
            port = buf.readUnsignedShort();
            buf.skipBytes(4);   // IPv6 flow information.
            buf.readBytes(binary);
            buf.skipBytes(4);   // IPv6 scope id

        } else {
            return null;
        }

        try {
            return new InetSocketAddress(InetAddress.getByAddress(binary), port);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    public static String readString(ByteBuf buf) {
        int len = buf.readShort();
        byte[] dst = new byte[len];
        buf.readBytes(dst);
        return new String(dst, CharsetUtil.UTF_8);
    }

    public static void writeByte(ByteBuf buf, MessageIdentifier id) {
        buf.writeByte(id.ordinal());
    }

    public static void writeAddress(ByteBuf buf, InetSocketAddress address) {
        InetAddress addr = address.getAddress();
        byte[] binary = addr.getAddress();
        int port = address.getPort();

        if (addr instanceof Inet4Address) {
            // ip version is 4
            buf.writeByte(4);

            for (int i = 0; i < 4; i++) {
                int b = ~binary[i];
                buf.writeByte(b);
            }

            buf.writeShort(port);

        } else if (addr instanceof Inet6Address) {
            // ip version is 6
            buf.writeByte(6);

            //typedef struct sockaddr_in6 {
            //    ADDRESS_FAMILY sin6_family; // AF_INET6.
            buf.writeShort(10);               // Todo: check if it's 10 (linux/socket.h) or 23 (winsocks2.h)
            //    USHORT sin6_port;           // Transport level port number.
            buf.writeShort(port);
            //    ULONG  sin6_flowinfo;       // IPv6 flow information.
            buf.writeInt(0);
            //    IN6_ADDR sin6_addr;         // IPv6 address.
            buf.writeBytes(binary);
            //    ULONG sin6_scope_id;        // Set of interfaces for a scope.
            buf.writeInt(0);
            //} SOCKADDR_IN6_LH, *PSOCKADDR_IN6_LH, FAR *LPSOCKADDR_IN6_LH;
        }
    }

    public static void writeString(ByteBuf buf, String str) {
        buf.writeShort(str.length());
        buf.writeBytes(str.getBytes(CharsetUtil.UTF_8));
    }

    public static void padWithZero(ByteBuf buf, int bytes) {
        int numToWrite = bytes - buf.writerIndex();
        buf.writeZero(numToWrite);
    }

    public static int bitToBytes(int bitLength) {
        return (bitLength + 7) >> 3;
    }

    public static int bytesToBits(int byteLength) {
        return byteLength << 3;
    }

    public static int getHeaderLength(InternalPacket packet) {
        PacketReliability reliability = packet.reliability;

        int len = 1 + 2; // flag + bit length

        if (reliability.isReliable()) {
            len += 3; // reliable index
        }

        if (reliability.isSequenced()) {
            len += 3; // sequence index
        }

        if (reliability.isOrdered()) {
            len += 3; // ordering index
            len += 1; // ordering channel
        }

        if (packet.splitPacketCount > 0) {
            len += 4; // split packet count
            len += 2; // split packet id
            len += 4; // split packet index
        }

        return len;

    }
}
