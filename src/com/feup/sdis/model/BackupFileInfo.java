package com.feup.sdis.model;

import java.util.Hashtable;

public class BackupFileInfo {

    final private String fileID;
    final private String originalFilename;
    final private String originalPath;
    final private int nChunks;
    final private Hashtable<Integer, String> restoredChunks = new Hashtable();

    public BackupFileInfo(String fileID, String originalFilename, String originalPath, int nChunks) {
        this.fileID = fileID;
        this.originalFilename = originalFilename;
        this.originalPath = originalPath;
        this.nChunks = nChunks;
    }

    public String getfileID() { return fileID; }

    public String getOriginalFilename() { return originalFilename; }

    public String getOriginalPath() { return originalPath; }

    public int getNChunks() {
        return nChunks;
    }

    public Hashtable<Integer, String> getRestoredChunks() {
        return restoredChunks;
    }

    public boolean isFullyRestored() {
        return restoredChunks.size() == nChunks;
    }

}
