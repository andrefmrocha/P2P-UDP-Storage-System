package com.feup.sdis.model;

import java.util.HashSet;
import java.util.Set;

public class BackupFileInfo {

    final private String fileID;
    final private int nChunks;
    final private Set<Integer> restoredChunks = new HashSet<>();

    public BackupFileInfo(String fileID, int nChunks) {
        this.fileID = fileID;
        this.nChunks = nChunks;
    }

    public String getfileID() { return fileID; }

    public int getNChunks() {
        return nChunks;
    }

    public Set<Integer> getRestoredChunks() {
        return restoredChunks;
    }

    public boolean isFullyRestored() {
        return restoredChunks.size() == nChunks;
    }

}
