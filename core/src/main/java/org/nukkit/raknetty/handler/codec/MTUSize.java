package org.nukkit.raknetty.handler.codec;


public class MTUSize {

    // 17914. 16 Mbit/Sec Token Ring
    // 4464.  4 Mbits/Sec Token Ring
    // 4352.  FDDI
    // 1500.  The largest Ethernet packet size \b recommended. This is the typical setting for non-PPPoE, non-VPN connections. The default value for NETGEAR routers, adapters and switches.
    // 1492.  The size PPPoE prefers.
    // 1472.  Maximum size to use for pinging. (Bigger packets are fragmented.)
    // 1468.  The size DHCP prefers.
    // 1460.  Usable by AOL if you don't have large email attachments, etc.
    // 1430.  The size VPN and PPTP prefer.
    // 1400.  Maximum size for AOL DSL.
    // 576.   Typical value to connect to dial-up ISPs.

    public static final int MAXIMUM_MTU_SIZE = 1492;
    public static final int MINIMUM_MTU_SIZE = 400;
}
