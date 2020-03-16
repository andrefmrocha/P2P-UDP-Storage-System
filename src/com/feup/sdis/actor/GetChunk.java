package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class GetChunk extends MessageActor {
    final static public String type = "GETCHUNK";

    public GetChunk(Message message) {
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

        if (Store.instance().getStoredFiles().contains(chunkId)) {
            File chunkFile = new File(Constants.SENDER_ID + "/" + chunkId);
            final String fileContent = new String(Files.readAllBytes(chunkFile.toPath()), StandardCharsets.UTF_8);

            final Header sendingHeader = new Header(
                    Constants.version,
                    Chunk.type, Constants.SENDER_ID,
                    fileID, Integer.parseInt(chunkNo));

            final Message msg = new Message(sendingHeader, fileContent);
            this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, msg);
        }
    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
