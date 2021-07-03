package org.nukkit.raknetty.channel.nio;

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
    public InetSocketAddress remoteAddress() {
        return parent().remoteAddress();
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return (AbstractUnsafe) parent().unsafe();
    }
}
