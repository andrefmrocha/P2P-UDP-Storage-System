package com.feup.sdis.actor;

import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;

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
        int chunkNo = Integer.parseInt(msgHeader.getChunkNo());

        if(message.getBody().length == 0){
            System.out.println("Chunk number " + chunkNo + " of file " + fileID + " has an empty body");
            return;
        }


        int chunkSize = message.getBody().length;
        Store store = Store.instance();

        int diskSpaceLimit = store.getMaxDiskSpace();
        int usedDiskSpace = store.getUsedDiskSpace();
        boolean fitsDisk = (diskSpaceLimit == Constants.unlimitedDiskSpace || (usedDiskSpace + chunkSize <= diskSpaceLimit));
        if (store.getStoredFiles().containsKey(chunkId)) {
            System.out.println("Already stored chunk " + chunkId);
            this.sendStored(msgHeader);
            return;
        }
        if (!fitsDisk) {
            System.out.println("No available disk space for chunk");
            return;
        }

        if(!this.checkReplDegree(chunkId, msgHeader)){
            System.out.println("Replication degree for chunk " + chunkId + " has already been hit");
            return;
        }

        // store relevant information
        int desiredReplicationDegree = msgHeader.getReplicationDeg();
        store.getStoredFiles().put(chunkId, new StoredChunkInfo(fileID, desiredReplicationDegree, chunkNo, chunkSize));

        // update own replication count
        final ReplicationCounter replCounter = Store.instance().getReplCount();
        replCounter.addNewID(chunkId, Constants.SENDER_ID);

        // write chunk to disk
        try (FileOutputStream fos = new FileOutputStream(Constants.backupFolder + chunkId)) {
            fos.write(message.getBody());
        }
        this.sendStored(msgHeader);

    }

    private boolean checkReplDegree(String chunkId, Header msgHeader) {
        return msgHeader.getVersion().equals(Constants.version) ||
                (msgHeader.getVersion().equals(Constants.enhancedVersion)
                        && Store.instance().getReplCount().getSize(chunkId) < msgHeader.getReplicationDeg());
    }

    private void sendStored(Header msgHeader) {
        final Header sendingHeader = new Header(
                msgHeader.getVersion(),
                Stored.type, Constants.SENDER_ID,
                msgHeader.getFileId(), Integer.parseInt(msgHeader.getChunkNo()),
                msgHeader.getReplicationDeg());
        final Message message = new Message(sendingHeader);
        System.out.println("Stored chunk " + msgHeader.getChunkId() + ", sending STORED msg");
        this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
