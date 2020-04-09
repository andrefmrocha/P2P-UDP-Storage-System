package com.feup.sdis.model;

import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class BackupFileInfo {

    final private String fileID;
    final private String originalFilename;
    final private String originalPath;
    final private int nChunks;
    final private SortedMap<Integer, byte[]> restoredChunks = new ConcurrentSkipListMap<>();

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

    public SortedMap<Integer, byte[]> getRestoredChunks() {
        return restoredChunks;
    }

    public boolean isFullyRestored() {
        return restoredChunks.size() == nChunks;
    }

}
