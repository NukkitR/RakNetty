package org.nukkit.raknetty.channel.nio;

import io.netty.channel.ChannelPromise;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

class NioRakServerConnectorChannel extends NioRakChannel {

    public NioRakServerConnectorChannel(NioRakServerChannel parent) {
        super(parent);
    }

    @Override
    protected void doClose() throws Exception {
        super.doClose();
        parent().removeChildChannel(remoteAddress());
    }

    @Override
    protected boolean doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doFinishConnect() throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public long localGuid() {
        return parent().localGuid();
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new AbstractUnsafe() {
            @Override
            public void connect(SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
