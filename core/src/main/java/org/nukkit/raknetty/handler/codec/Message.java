package org.nukkit.raknetty.handler.codec;

public interface Message extends ByteBufSerializable {

    int RAKNET_PROTOCOL_VERSION = 10;

    int UDP_HEADER_SIZE = 28;

    int MESSAGE_HEADER_MAX_SIZE = 1 + 2 + 3 + 3 + 3 + 1 + 4 + 2 + 4;

    MessageIdentifier getId();
}
