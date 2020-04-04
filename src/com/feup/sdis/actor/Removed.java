package com.feup.sdis.actor;

import com.feup.sdis.actions.Backup;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        if (currReplDegree == null) {
            System.out.println("[REMOVED] Did not find chunk in replCount");
            return;
        }

        currReplDegree.remove(message.getHeader().getChunkId());
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
