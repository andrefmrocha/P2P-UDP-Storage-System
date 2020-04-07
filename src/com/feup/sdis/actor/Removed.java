package com.feup.sdis.actor;

import com.feup.sdis.actions.Backup;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Removed extends MessageActor {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    final static public String type = "REMOVED";
    private final Random random = new Random();

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
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();

        // update repl count on all peers
        final SerializableHashMap replCount = Store.instance().getReplCount();
        final Set<String> currReplDegree = replCount.get(chunkId);
        if(currReplDegree == null) {
            System.out.println("[REMOVED] Did not find chunk in replCount");
            return;
        }

        currReplDegree.remove(message.getHeader().getSenderId());
        replCount.put(chunkId, currReplDegree);

        // if peer has copy of the chunk
        if (storedFiles.containsKey(chunkId)) {

            final StoredChunkInfo stored = storedFiles.get(chunkId);
            if (currReplDegree.size() < stored.getDesiredReplicationDegree()) {

                int replDeg = stored.getDesiredReplicationDegree();

                try {
                    Backup.sendPutChunk((new File(Constants.SENDER_ID + "/" + Constants.backupFolder + chunkId)).toPath(),
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
