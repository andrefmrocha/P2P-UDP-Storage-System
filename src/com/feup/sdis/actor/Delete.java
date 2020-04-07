package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
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
                final File file = new File(Constants.SENDER_ID + "/" + Constants.backupFolder + chunkId);
                if(!file.delete()){
                    System.out.println("Failed to delete chunk " + chunkId);
                }
                storedFiles.remove(chunkId);
                if(message.getHeader().getVersion().equals(Constants.enhancedVersion)){
                    final Header msgHeader = message.getHeader();
                    final Header sendingHeader = new Header(
                            Constants.enhancedVersion,
                            Deleted.type, Constants.SENDER_ID,
                            msgHeader.getFileId(), Integer.parseInt(msgHeader.getChunkNo()),
                            msgHeader.getReplicationDeg());

                    final Message message = new Message(sendingHeader);
                    this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, message);
                }

                Store.instance().getReplCount().remove(chunkId);
            }
        }

    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
