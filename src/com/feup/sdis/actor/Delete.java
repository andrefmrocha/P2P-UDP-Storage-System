package com.feup.sdis.actor;

public class Delete implements MessageType {
    static public String getType() {
        return "DELETE";
    }

    @Override
    public String process() {
        return null;
    }
}
