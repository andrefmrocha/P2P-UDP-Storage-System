package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
    public void process(Map<String, Integer> files) throws IOException {
        final String fileId = message.getHeader().getFileId();
        if (!files.containsKey(fileId)) {
            files.put(fileId, 1);
            PrintWriter fileOutputStream = new PrintWriter(Constants.SENDER_ID + "/" + fileId);
            fileOutputStream.write(message.getBody());
            fileOutputStream.close();
        }

        final Header sendingHeader = new Header(
                Constants.version,
                Stored .type, Constants.SENDER_ID,
                fileId, Integer.parseInt(message.getHeader().getChunkNo()),
                message.getHeader().getReplicationDeg());

        this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, sendingHeader);
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
