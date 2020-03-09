package com.feup.sdis.actor;

public class Stored implements MessageType {
    static public String getType() {
        return "STORED";
    }

    @Override
    public String process() {
        return null;
    }
}
