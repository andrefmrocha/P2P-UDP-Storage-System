package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.util.HashSet;

public class Excess extends MessageActor {
    final static public String type = "EXCESS";

    public Excess(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        if (!message.getHeader().getExtraParam().equals(Constants.SENDER_ID)) {
            System.out.println("This was not target peer, ignoring Excess msg");
            return;
        }
        final String chunkId = message.getHeader().getChunkId();
        final File file = new File(Constants.backupFolder + chunkId);
        if (!file.delete()) {
            System.out.println("Failed to delete chunk " + chunkId);
        }

        Store.instance().getReplCount().removeID(chunkId, Constants.SENDER_ID);
        Store.instance().getStoredFiles().remove(chunkId);
        System.out.println("Deleted chunk " + chunkId);
    }

    @Override
    boolean hasBody() {
        return false;
    }
}
