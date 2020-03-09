package com.feup.sdis.actor;

public class Chunk implements MessageType {
    final static public String type =  "CHUNK";
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String process() {
        return null;
    }
}
