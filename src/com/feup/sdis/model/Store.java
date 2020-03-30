package com.feup.sdis.model;

import java.util.*;
import com.feup.sdis.peer.Constants;

import java.util.HashSet;
import java.util.Set;

public class Store {
    private static Store storeInstance;
    final private SerializableHashMap replCount = new SerializableHashMap(Constants.SENDER_ID + "/" + "files.ser");
    //TODO: Check if this is the best approach
    final private SortedMap<String, BackupFileInfo> backedUpFiles = new TreeMap<>();
    final private SortedMap<String, StoredChunkInfo> storedFiles = new TreeMap<>();

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

}
