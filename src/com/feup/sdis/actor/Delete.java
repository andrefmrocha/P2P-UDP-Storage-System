package com.feup.sdis.actor;

import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.util.Set;

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
        final Set<String> storedFiles = Store.instance().getStoredFiles();
        final String fileId = message.getHeader().getFileId();
        for(int i = 0; i < 1000; i++){
            if(storedFiles.contains(fileId+i)){
                final File file = new File(Constants.SENDER_ID + "/" + fileId+i);
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
