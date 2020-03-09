package com.feup.sdis.actor;

public class Removed implements MessageType {
    final static public String type =  "REMOVED";
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String process() {
        return null;
    }
}
