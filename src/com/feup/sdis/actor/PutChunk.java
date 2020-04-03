package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;
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
        final String chunkId = msgHeader.getChunkId();
        int chunkSize = message.getBody().length();
        Store store = Store.instance();

        int diskSpaceLimit = store.getMaxDiskSpace();
        int usedDiskSpace = store.getUsedDiskSpace();
        boolean fitsDisk = (diskSpaceLimit == Constants.unlimitedDiskSpace || (usedDiskSpace+chunkSize <= diskSpaceLimit));
        if (!store.getStoredFiles().containsKey(chunkId) && fitsDisk) {
            // store relevant information
            String[] parts = chunkId.split(""+Constants.idSeparation);
            if(parts.length != 2) {
                System.out.println("Chunk ID malformed: " + chunkId);
                return;
            }
            String fileID = parts[0];
            int desiredReplicationDegree = msgHeader.getReplicationDeg();
            int chunkNo = Integer.parseInt(parts[1]);
            store.getStoredFiles().put(chunkId, new StoredChunkInfo(fileID, desiredReplicationDegree, chunkNo, chunkSize));

            PrintWriter fileOutputStream = new PrintWriter(Constants.SENDER_ID + "/" + Constants.backupFolder + chunkId);
            fileOutputStream.write(message.getBody());
            fileOutputStream.close();

            final Header sendingHeader = new Header(
                    Constants.version,
                    Stored .type, Constants.SENDER_ID,
                    msgHeader.getFileId(), Integer.parseInt(msgHeader.getChunkNo()),
                    msgHeader.getReplicationDeg());

            final Message message = new Message(sendingHeader);
            this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);
        }

    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
