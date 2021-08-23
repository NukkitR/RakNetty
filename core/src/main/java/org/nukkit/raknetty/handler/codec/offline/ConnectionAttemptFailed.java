package org.nukkit.raknetty.handler.codec.offline;

import org.nukkit.raknetty.handler.codec.MessageIdentifier;
import org.nukkit.raknetty.handler.codec.OfflineMessageAdapter;

public class ConnectionAttemptFailed extends OfflineMessageAdapter {

    @Override
    public MessageIdentifier getId() {
        return MessageIdentifier.ID_CONNECTION_ATTEMPT_FAILED;
    }
}
