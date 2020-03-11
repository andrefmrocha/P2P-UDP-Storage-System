package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public abstract class MessageActor {
    protected final Message message;

    protected MessageActor(Message message) {
        this.message = message;
    }

    public static byte[] parseBody(String msg){
        return msg.substring(msg.indexOf("\n\n")).getBytes();
    }

    abstract String getType();
    public abstract void process(Set<UUID> files) throws IOException;
    abstract boolean hasBody();
}
