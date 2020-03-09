package com.feup.sdis.actor;

public class Removed implements MessageType {
    static public String getType() {
        return "REMOVED";
    }


    @Override
    public String process() {
        return null;
    }
}
