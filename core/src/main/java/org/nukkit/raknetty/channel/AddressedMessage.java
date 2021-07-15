package org.nukkit.raknetty.channel;

import io.netty.channel.DefaultAddressedEnvelope;
import org.nukkit.raknetty.handler.codec.Message;

import java.net.InetSocketAddress;

public class AddressedMessage extends DefaultAddressedEnvelope<Message, InetSocketAddress> {
    public AddressedMessage(Message message, InetSocketAddress recipient, InetSocketAddress sender) {
        super(message, recipient, sender);
    }

    public AddressedMessage(Message message, InetSocketAddress recipient) {
        super(message, recipient);
    }
}
