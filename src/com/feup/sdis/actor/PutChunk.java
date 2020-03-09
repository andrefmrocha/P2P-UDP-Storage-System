package com.feup.sdis.actor;

public class PutChunk implements MessageType {
    static public String getType() {
        return "PUTCHUNK";
    }

    @Override //TODO: Implement PUTCHUNKS
    public String process() {
        return null;
    }
}
