package com.feup.sdis.actor;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.io.PrintWriter;

public class Chunk extends MessageActor {
    final static public String type =  "CHUNK";

    public Chunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException {
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        final String chunkContent = message.getBody();

        BackupFileInfo localInfo = Store.instance().getBackedUpFiles().get(fileID);
        if(localInfo == null) {
            System.out.println("Did not find backed up file info");
            return;
        }

        if (!localInfo.getRestoredChunks().contains(chunkNo)) {
            localInfo.getRestoredChunks().put(chunkNo, chunkContent);

            if(localInfo.isFullyRestored()) {
                String fileContent = "";
                for (String chunk : localInfo.getRestoredChunks().values())
                    fileContent += chunk;

                PrintWriter fileOutputStream = new PrintWriter(Constants.SENDER_ID + "/" +
                                                                    Constants.restoredFolder +
                                                                    localInfo.getOriginalFilename());
                fileOutputStream.write(fileContent);
                fileOutputStream.close();
            }
        }
        else System.out.println("Chunk already retrieved!");
    }

    @Override
    public boolean hasBody() {
        return true;
    }
}
