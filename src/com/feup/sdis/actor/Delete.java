package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.util.Map;
import java.util.SortedMap;

public class Delete extends MessageActor {
    final static public String type =  "DELETE";

    public Delete(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();
        final String fileId = message.getHeader().getFileId();

        for(Map.Entry<String,StoredChunkInfo> entry : storedFiles.entrySet()) {
            String chunkId = entry.getKey();
            if(chunkId.startsWith(fileId)) {
                System.out.println("Deleting chunk " + chunkId);
                final File file = new File(Constants.backupFolder + chunkId);
                if(!file.delete()){
                    System.out.println("Failed to delete chunk " + chunkId);
                }
                storedFiles.remove(chunkId);
                Store.instance().getReplCount().remove(chunkId);
            }
        }

    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
