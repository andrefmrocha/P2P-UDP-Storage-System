package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
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
        final String chunkId = message.getHeader().getChunkId();

        if(!Store.instance().getStoredFiles().containsKey(chunkId)) {
            System.out.println("Do not have chunk " + chunkId + " stored");
            return;
        }
        File chunkFile = new File(Constants.backupFolder + chunkId);
        final byte[] fileContent = Files.readAllBytes(chunkFile.toPath());
        System.out.println("Sending CHUNK msg for chunk " + chunkId);
        sendFile(fileID, chunkNo, fileContent);
    }

    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        final Header sendingHeader = new Header(
                Constants.version,
                Chunk.type, Constants.SENDER_ID,
                fileID, Integer.parseInt(chunkNo));

        final Message msg = new Message(sendingHeader, fileContent);
        this.sendMessage(Constants.MC_PORT, Constants.MDR_CHANNEL, msg);
    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
