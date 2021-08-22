package org.nukkit.raknetty.channel.bedrock;

import io.netty.channel.ChannelException;

public class LoginException extends ChannelException {

    public LoginException() {
    }

    public LoginException(String message, Throwable cause) {
        super(message, cause);
    }

    public LoginException(String message) {
        super(message);
    }

    public LoginException(Throwable cause) {
        super(cause);
    }

}
