package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;

public class Deleted extends MessageActor {
    final static public String type = "DELETED";

    public Deleted(Message message) {
        super(message);
    }

    @Override
    String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId = message.getHeader().getChunkId();
        final String peerId = message.getHeader().getSenderId();
        final SerializableHashMap replCounter = Store.instance().getReplCount();
        if (replCounter.containsPeer(chunkId, peerId)) {
            replCounter.removeID(chunkId, peerId);
            System.out.println("Updated replication table for chunk " + chunkId + ", removed peer " + peerId);
        }

    }

    @Override
    boolean hasBody() {
        return false;
    }
}
