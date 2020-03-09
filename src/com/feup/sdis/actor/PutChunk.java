package com.feup.sdis.actor;

public class PutChunk implements MessageType {
    final static public String type =  "PUTCHUNK";

    @Override
    public String getType() {
        return type;
    }

    @Override //TODO: Implement PUTCHUNKS
    public String process() {
        return null;
    }
}
