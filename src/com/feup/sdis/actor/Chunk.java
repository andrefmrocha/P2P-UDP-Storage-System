package com.feup.sdis.actor;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.*;

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
        final byte[] chunkContent = message.getBody();
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        BackupFileInfo localInfo = Store.instance().getBackedUpFiles().get(fileID);
        if(!isFileBackedUp(localInfo)) {
            System.out.println("This peer was not the Restore initiator");
            return;
        }

        if(localInfo.isFullyRestored()) {
            System.out.println("File is already fully restored");
            return;
        }

        if(isChunkRestored(localInfo, chunkNo)) {
            System.out.println("Chunk " + chunkNo + " already restored");
            return;
        }

        storeFile(chunkContent, chunkNo, localInfo);
    }

    protected void storeFile(byte[] chunkContent, int chunkNo, BackupFileInfo localInfo) throws IOException {
        String fileID = localInfo.getfileID();
        System.out.println("Storing chunk no. " + chunkNo + " for file " + fileID);
        localInfo.getRestoredChunks().put(chunkNo, chunkContent);

        if ( localInfo.isFullyRestored()) {
            System.out.println("File " + fileID + " fully restored, writing to disk as " + localInfo.getOriginalFilename());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
            for (byte[] chunk : localInfo.getRestoredChunks().values())
                outputStream.write(chunk);

            try (FileOutputStream fos = new FileOutputStream(Constants.restoredFolder + localInfo.getOriginalFilename())) {
                fos.write(outputStream.toByteArray());
            }
        }
    }

    protected boolean isChunkRestored(BackupFileInfo localInfo, int chunkNo) {
        return localInfo.getRestoredChunks().get(chunkNo) != null;
    }

    protected boolean isFileBackedUp(BackupFileInfo localInfo) {
        return !(localInfo == null);
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
