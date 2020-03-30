package com.feup.sdis.actions;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Store;

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

        final Set<String> storedFiles = Store.instance().getStoredFiles();
        message += "Stored files:\n";
        for (String file: storedFiles){
            message += "• " + file;
        }
        return message;
    }
}
