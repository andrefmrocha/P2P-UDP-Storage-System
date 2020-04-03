package com.feup.sdis.actions;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Store;
import com.feup.sdis.model.StoredChunkInfo;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class State implements Action {
    @Override
    public String process() {
        String message = "Backed up files: \n";
        final SortedMap<String, BackupFileInfo> backedFiles = Store.instance().getBackedUpFiles();
        for(Map.Entry<String, BackupFileInfo> entry : backedFiles.entrySet()) {
            final BackupFileInfo file = entry.getValue();
            message += " - " + file.getfileID() + ": " + file.getOriginalFilename() + "\n  "
                    +  " • " + "path: " + file.getOriginalPath() + "\n  "
                    + " • " + "no chunks: " + file.getNChunks() + "\n";
        }

        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();
        message += "Stored files:\n";
        for (Map.Entry<String, StoredChunkInfo> entry: storedFiles.entrySet()){
            message += "• " + entry.getKey();
            message += " - fileID :" + entry.getValue().getFileID();
            message += " - desiredReplicationDegree :" + entry.getValue().getDesiredReplicationDegree();
            message += " - chunkNo :" + entry.getValue().getChunkNo();
            message += " - chunkSize :" + entry.getValue().getChunkSize();
        }
        return message;
    }
}
