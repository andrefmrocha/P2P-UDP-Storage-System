package com.feup.sdis.actor;

public class Delete implements MessageType {
    final static public String type =  "DELETE";
    @Override
    public String getType() {
        return type;
    }

    @Override
    public String process() {
        return null;
    }
}
