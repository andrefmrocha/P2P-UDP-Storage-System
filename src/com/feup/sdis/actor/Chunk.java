package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.IOException;

public class Chunk extends MessageActor {
    final static public String type =  "CHUNK";

    public Chunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException { //TODO: Make Process
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
