package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;

import java.util.HashSet;
import java.util.Set;

public class Stored extends MessageActor {
    final static public String type = "STORED";

    public Stored(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId = message.getHeader().getChunkId();
        final SerializableHashMap replCounter = Store.instance().getReplCount();
        final Set<String> currentReplications =
                replCounter.getOrDefault(chunkId, new HashSet<>());
        if (!currentReplications.contains(message.getHeader().getSenderId())){
            currentReplications.add(message.getHeader().getSenderId());
        }
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
