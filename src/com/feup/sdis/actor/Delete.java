package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

public class Delete extends MessageActor {
    final static public String type =  "DELETE";

    public Delete(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process(Map<String, Integer> files) throws IOException {//TODO: Make Process

    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
