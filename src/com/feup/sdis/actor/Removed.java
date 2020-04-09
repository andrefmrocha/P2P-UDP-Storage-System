package com.feup.sdis.actor;

import com.feup.sdis.actions.Backup;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Removed extends MessageActor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    final static public String type = "REMOVED";

    public Removed(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId = message.getHeader().getChunkId();
        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();

        // update repl count on all peers
        final SerializableHashMap replCount = Store.instance().getReplCount();
        if(!replCount.contains(chunkId)) {
            System.out.println("[REMOVED] Did not find chunk in replCount");
            return;
        }

        replCount.removeID(chunkId, message.getHeader().getSenderId());
        System.out.println("Updated replication table for chunk " + chunkId + ", removed peer " + message.getHeader().getSenderId());

        // if peer has copy of the chunk
        if (storedFiles.containsKey(chunkId)) {

            final StoredChunkInfo stored = storedFiles.get(chunkId);
            if (replCount.getSize(chunkId) < stored.getDesiredReplicationDegree()) {
                System.out.println("Replication degree dropped below the desired level");
                System.out.println("Starting backup protocol for chunk " + chunkId);
                int replDeg = stored.getDesiredReplicationDegree();

                try {
                    Backup.sendPutChunk(new File(Constants.backupFolder + chunkId),
                                        replDeg, scheduler);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
