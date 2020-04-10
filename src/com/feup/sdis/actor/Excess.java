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
        final String chunkId = message.getHeader().getChunkId();
        final String extraParam = message.getHeader().getExtraParam();
        if (!extraParam.equals(Constants.SENDER_ID)) {
            System.out.println("Updated replication table for chunk " + chunkId + ", removed peer " + extraParam);
            Store.instance().getReplCount().removeID(chunkId, extraParam);
            return;
        }
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
