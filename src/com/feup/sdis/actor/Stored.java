package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;

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
        final Integer currentReplications =
                replCounter.getOrDefault(chunkId, 0);
        replCounter.put(chunkId, currentReplications + 1);
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
