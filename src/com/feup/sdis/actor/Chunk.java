package com.feup.sdis.actor;

public class Chunk implements MessageType {
    static public String getType() {
        return "CHUNK";
    }


    @Override
    public String process() {
        return null;
    }
}
