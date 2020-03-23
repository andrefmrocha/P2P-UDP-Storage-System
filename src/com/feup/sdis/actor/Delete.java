package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.util.Set;
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
        final SortedMap<String, Integer> storedFiles = Store.instance().getStoredFiles();
        final String fileId = message.getHeader().getFileId();
        for(int i = 0; i < 1000; i++){ // TODO remove magic value
            if(storedFiles.containsKey(fileId+i)){
                final File file = new File(Constants.SENDER_ID + "/" + Constants.backupFolder + fileId+i);
                if(!file.delete()){
                    System.out.println("Failed to delete file " + fileId + i);
                }
                storedFiles.remove(fileId+i);
            }
        }

    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
