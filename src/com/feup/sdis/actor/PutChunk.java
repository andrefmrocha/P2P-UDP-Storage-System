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
        final String chunkId = message.getHeader().getChunkId();
        if (!Store.instance().getStoredFiles().containsKey(chunkId)) {
            Store.instance().getStoredFiles().put(chunkId, message.getBody().length());
            PrintWriter fileOutputStream = new PrintWriter(Constants.SENDER_ID + "/" + chunkId);
            fileOutputStream.write(message.getBody());
            fileOutputStream.close();

            final Header sendingHeader = new Header(
                    Constants.version,
                    Stored .type, Constants.SENDER_ID,
                    message.getHeader().getFileId(), Integer.parseInt(message.getHeader().getChunkNo()),
                    message.getHeader().getReplicationDeg());

            final Message message = new Message(sendingHeader);
            this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);
        }

    }

    // TODO must store chunk no and desired replication degree

    @Override
    public boolean hasBody() {
        return true;
    }
}
