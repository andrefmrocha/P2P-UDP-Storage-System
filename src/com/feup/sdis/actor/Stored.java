package com.feup.sdis.actor;

public class Stored implements MessageType {
    final static public String type =  "STORED";

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String process() {
        return null;
    }
}
