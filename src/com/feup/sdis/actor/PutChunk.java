package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class PutChunk extends MessageActor {
    final static public String type =  "PUTCHUNK";

    public PutChunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process(Set<UUID> files) throws IOException { //TODO: Make Process

    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
