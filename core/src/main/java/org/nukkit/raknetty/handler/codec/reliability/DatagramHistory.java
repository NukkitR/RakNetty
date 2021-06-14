package org.nukkit.raknetty.handler.codec.reliability;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatagramHistory {

    private final int datagramHistoryIndex = 0;
    private final Map<Integer, Node> histories = new HashMap<>();

    public Node get(int datagramNumber) {
        if (datagramNumber < datagramHistoryIndex) {
            return null;
        }

        return histories.get(datagramNumber);
    }

    public void remove(int datagramNumber) {
        histories.remove(datagramNumber);
    }

    public static final class Node {
        public LinkedList<Integer> messages;
        public long timeSent;

        public Node(long timeSent) {
            this.timeSent = timeSent;
        }
    }
}
