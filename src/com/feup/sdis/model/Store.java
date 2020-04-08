package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Store {
    private static Store storeInstance;
    final private SerializableHashMap replCount = new SerializableHashMap(Constants.peerRootFolder + "files.ser");
    //TODO Check if this is the best approach
    final private SortedMap<String, BackupFileInfo> backedUpFiles = new TreeMap<>();
    final private SortedMap<String, StoredChunkInfo> storedFiles = new TreeMap<>();
    private int maxDiskSpace = Constants.unlimitedDiskSpace;

    private Store(){}

    public static Store instance(){
        if(storeInstance == null){
            storeInstance = new Store();
        }
        return storeInstance;
    }

    public synchronized SortedMap<String, BackupFileInfo> getBackedUpFiles() { return backedUpFiles; }
    public synchronized SerializableHashMap getReplCount() {
        return replCount;
    }
    public synchronized SortedMap<String, StoredChunkInfo> getStoredFiles() {
        return storedFiles;
    }

    public synchronized int getMaxDiskSpace() {
        return maxDiskSpace;
    }

    public synchronized void setMaxDiskSpace(int limit) {
        maxDiskSpace = limit;
    }

    public synchronized int getUsedDiskSpace() {
        int total = 0;
        for(Map.Entry<String,StoredChunkInfo> entry : storedFiles.entrySet()) {
            total += entry.getValue().chunkSize;
        }
        return total;
    }
}
