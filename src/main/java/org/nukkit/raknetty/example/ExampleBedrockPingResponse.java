package org.nukkit.raknetty.example;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.nukkit.raknetty.channel.RakServerChannel;
import org.nukkit.raknetty.handler.codec.offline.OfflinePingResponse;
import org.nukkit.raknetty.util.RakNetUtil;

public class ExampleBedrockPingResponse implements OfflinePingResponse {

    private int numOfPlayers = -1;
    private ByteBuf data;
    //MCPE;Dedicated Server;448;1.17.10;0;10;11877191924423115074;Bedrock level;Survival;1;19132;19133;
    private static final String format = "MCPE;RakNetty Server;448;1.17.10;%d;%d;%d;Bedrock level;Survival;1;%d;0;";

    @Override
    public ByteBuf get(RakServerChannel channel) {
        int numOfConnections = channel.numberOfConnections();
        if (data == null) {
            data = Unpooled.buffer();
        }

        if (numOfConnections != numOfPlayers) {
            data.clear();
            int maxConnections = channel.config().getMaximumConnections();
            long guid = channel.localGuid();
            int port = channel.localAddress().getPort();
            String msg = String.format(format, numOfConnections, maxConnections, guid, port);
            RakNetUtil.writeString(data, msg);
        }
        return data;
    }
}
