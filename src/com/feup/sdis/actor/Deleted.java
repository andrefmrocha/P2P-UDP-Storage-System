package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
        final Set<String> currentReplications =
                replCounter.getOrDefault(chunkId, new HashSet<>());
        if (currentReplications.contains(peerId)){
            currentReplications.remove(peerId);
            replCounter.put(chunkId, currentReplications);
        }

    }

    @Override
    boolean hasBody() {
        return false;
    }
}
