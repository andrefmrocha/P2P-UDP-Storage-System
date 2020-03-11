package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class GetChunk extends MessageActor {
    final static public String type =  "GETCHUNK";

    public GetChunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process(Map<UUID, Integer> files) throws IOException {//TODO: Make Process

    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
