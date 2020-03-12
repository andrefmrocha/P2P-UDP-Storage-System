package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.util.Map;
import java.util.UUID;

public class Stored extends MessageActor {
    final static public String type =  "STORED";

    public Stored(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process(Map<String, Integer> files) {
        final String filedId = message.getHeader().getFileId();
        if(files.containsKey(filedId)){
            files.put(filedId, files.get(filedId) + 1);
        }
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
