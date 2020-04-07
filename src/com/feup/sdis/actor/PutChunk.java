package com.feup.sdis.actor;

import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.io.PrintWriter;

public class PutChunk extends MessageActor {
    final static public String type = "PUTCHUNK";

    public PutChunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException {
        final Header msgHeader = message.getHeader();
        final String fileID = msgHeader.getFileId();
        final String chunkId = msgHeader.getChunkId();
        int chunkSize = message.getBody().length();
        Store store = Store.instance();

        int diskSpaceLimit = store.getMaxDiskSpace();
        int usedDiskSpace = store.getUsedDiskSpace();
        boolean fitsDisk = (diskSpaceLimit == Constants.unlimitedDiskSpace || (usedDiskSpace+chunkSize <= diskSpaceLimit));

        if(store.getStoredFiles().containsKey(chunkId)) {
            System.out.println("Already stored chunk " + chunkId);
            // TODO sent stored either way, replcounter should store origin
            return;
        }
        if(!fitsDisk) {
            System.out.println("No available disk space for chunk");
            return;
        }

        // store relevant information
        int desiredReplicationDegree = msgHeader.getReplicationDeg();
        int chunkNo = Integer.parseInt(msgHeader.getChunkNo());
        store.getStoredFiles().put(chunkId, new StoredChunkInfo(fileID, desiredReplicationDegree, chunkNo, chunkSize));

        // update own replication count
        final SerializableHashMap replCounter = Store.instance().getReplCount();
        final Integer currentReplications = replCounter.getOrDefault(chunkId, 0);
        replCounter.put(chunkId, currentReplications + 1);

        // write chunk to disk
        PrintWriter fileOutputStream = new PrintWriter(Constants.backupFolder + chunkId);
        fileOutputStream.write(message.getBody());
        fileOutputStream.close();

        // send STORED message
        final Header sendingHeader = new Header(
                Constants.version,
                Stored.type, Constants.SENDER_ID,
                msgHeader.getFileId(), Integer.parseInt(msgHeader.getChunkNo()),
                msgHeader.getReplicationDeg());
        final Message message = new Message(sendingHeader);
        System.out.println("Stored chunk " + chunkId + ", sending STORED msg");
        this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);

    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
