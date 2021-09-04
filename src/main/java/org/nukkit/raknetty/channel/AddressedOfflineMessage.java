package org.nukkit.raknetty.channel;

import io.netty.channel.DefaultAddressedEnvelope;
import org.nukkit.raknetty.handler.codec.OfflineMessage;

import java.net.InetSocketAddress;

public class AddressedOfflineMessage extends DefaultAddressedEnvelope<OfflineMessage, InetSocketAddress> {
    public AddressedOfflineMessage(OfflineMessage message, InetSocketAddress recipient, InetSocketAddress sender) {
        super(message, recipient, sender);
    }

    public AddressedOfflineMessage(OfflineMessage message, InetSocketAddress recipient) {
        super(message, recipient);
    }
}
