package org.nukkit.raknetty.handler.codec.reliability;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class DatagramHistory {

    public static final int DATAGRAM_MESSAGE_ID_ARRAY_LENGTH = 512;

    private int datagramHistoryIndex = 0;
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

    public void add(int datagramNumber, int messageNumber) {
        Node node = histories.get(datagramNumber);

        if (node == null) {

            if (histories.size() > DATAGRAM_MESSAGE_ID_ARRAY_LENGTH) {
                histories.remove(datagramHistoryIndex);
                datagramHistoryIndex++;
            }

            long timeSent = System.nanoTime();
            node = new Node(timeSent);
            histories.put(datagramNumber, node);
        }

        node.messages.add(messageNumber);
    }

    public static final class Node {
        public LinkedList<Integer> messages = new LinkedList<>( );
        public long timeSent;

        public Node(long timeSent) {
            this.timeSent = timeSent;
        }
    }
}
