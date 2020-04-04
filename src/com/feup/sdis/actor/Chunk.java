package com.feup.sdis.actor;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

public class Chunk extends MessageActor {
    final static public String type = "CHUNK";

    public Chunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException {
        final String chunkContent = message.getBody();
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        BackupFileInfo localInfo = Store.instance().getBackedUpFiles().get(fileID);
        if (this.checkFile(localInfo) && this.checkChunk(localInfo, chunkNo))
            storeFile(chunkContent, chunkNo, localInfo);
        else System.out.println("Chunk already retrieved");
    }

    protected void storeFile(String chunkContent, int chunkNo, BackupFileInfo localInfo) throws FileNotFoundException {
        String fileID = localInfo.getfileID();
        System.out.println("Storing chunk no. " + chunkNo + " for file " + fileID);
        localInfo.getRestoredChunks().put(chunkNo, chunkContent);

        if ( localInfo.isFullyRestored()) {
            System.out.println("File " + fileID + " fully restored, writing to disk as " + localInfo.getOriginalFilename());
            StringBuilder fileContent = new StringBuilder();
            for (String chunk : localInfo.getRestoredChunks().values())
                fileContent.append(chunk);

            PrintWriter fileOutputStream = new PrintWriter(Constants.restoredFolder + localInfo.getOriginalFilename());
            fileOutputStream.println(fileContent.toString());
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }

    protected boolean checkChunk(BackupFileInfo localInfo, int chunkNo) {
        return !localInfo.getRestoredChunks().contains(chunkNo);
    }

    protected boolean checkFile(BackupFileInfo localInfo) {
        if (localInfo == null) {
            System.out.println("Did not find backed up file info");
            return false;
        }
        return true;
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
