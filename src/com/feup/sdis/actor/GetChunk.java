package com.feup.sdis.actor;

public class GetChunk implements MessageType {
    static public String getType() {
        return "GETCHUNK";
    }

    @Override
    public String process() {
        return null;
    }
}
