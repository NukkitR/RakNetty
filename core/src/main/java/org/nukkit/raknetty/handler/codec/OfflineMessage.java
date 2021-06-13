package org.nukkit.raknetty.handler.codec;

public interface OfflineMessage extends Message {

    byte[] OFFLINE_MESSAGE_DATA_ID = new byte[]{
            (byte) 0x00, (byte) 0xFF, (byte) 0xFF, (byte) 0x00,
            (byte) 0xFE, (byte) 0xFE, (byte) 0xFE, (byte) 0xFE,
            (byte) 0xFD, (byte) 0xFD, (byte) 0xFD, (byte) 0xFD,
            (byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78
    };
}
