package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
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
        final String fileID = message.getHeader().getFileId();
        final String chunkNo = message.getHeader().getChunkNo();
        final String chunkId = fileID + chunkNo;
        if (!Store.instance().getStoredFiles().contains(chunkId)) {
            Store.instance().getStoredFiles().add(chunkId);
            System.out.println(chunkId);

            PrintWriter fileOutputStream = new PrintWriter(Constants.SENDER_ID + "/" + Constants.backupFolder + chunkId);
            fileOutputStream.write(message.getBody());
            fileOutputStream.close();

            final Header sendingHeader = new Header(
                    Constants.version,
                    Stored .type, Constants.SENDER_ID,
                    fileID, Integer.parseInt(chunkNo),
                    message.getHeader().getReplicationDeg());

            final Message message = new Message(sendingHeader);
            this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);
        }

    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
