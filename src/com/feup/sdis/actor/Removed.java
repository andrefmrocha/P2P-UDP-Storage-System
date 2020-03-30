package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;

import java.io.IOException;
import java.util.SortedMap;

public class Removed extends MessageActor {
    final static public String type =  "REMOVED";

    public Removed(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException {
        // TODO peers that have a copy of chunk must initiate backup if drops below desired replication
        final String chunkId = message.getHeader().getChunkId();
        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();

        if(storedFiles.containsKey(chunkId)) {
            final SerializableHashMap replCount = Store.instance().getReplCount();
            final int currReplDegree = replCount.getOrDefault(chunkId, -99);
            if(currReplDegree == -99) {
                System.out.println("[REMOVED] Did not find chunk in replCount");
                return;
            }

            final int newReplDegree = currReplDegree - 1;
            replCount.put(chunkId, newReplDegree);

            final StoredChunkInfo stored = storedFiles.get(chunkId);
            if (newReplDegree < stored.getDesiredReplicationDegree()) {
                // TODO wait delay and start backup protocol
            }
        }
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
