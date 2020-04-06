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
        Store.instance().updateReplCount(message.getHeader().getChunkId(), 1);
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
