package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

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
    public void process(Map<UUID, Integer> files) throws IOException {
        final UUID fileId = message.getHeader().getFileId();
        if (!files.containsKey(fileId)) {
            files.put(fileId, 1);
            FileOutputStream fileOutputStream = new FileOutputStream(Constants.SENDER_ID + "/" + fileId);
            fileOutputStream.write(message.getBody());
        }

        final Header sendingHeader = new Header(
                Constants.version.toCharArray(),
                Stored .type, UUID.nameUUIDFromBytes(Constants.SENDER_ID.getBytes()),
                fileId, Integer.parseInt(new String(message.getHeader().getChunkNo())),
                message.getHeader().getReplicationDeg());

        this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, sendingHeader);
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
