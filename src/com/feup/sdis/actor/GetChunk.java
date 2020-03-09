package com.feup.sdis.actor;

public class GetChunk implements MessageType {
    final static public String type =  "GETCHUNK";
    @Override
    public String getType() {
        return type;
    }


    @Override
    public String process() {
        return null;
    }
}
