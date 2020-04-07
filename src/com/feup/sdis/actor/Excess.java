package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.File;

public class Excess extends MessageActor {
    final static public String type =  "EXCESS";

    public Excess(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId =  message.getHeader().getChunkId();
        final File file = new File(Constants.SENDER_ID + "/" + Constants.backupFolder + chunkId);
        if(!file.delete()){
            System.out.println("Failed to delete chunk " + chunkId);
        }
    }

    @Override
    boolean hasBody() {
        return false;
    }
}
