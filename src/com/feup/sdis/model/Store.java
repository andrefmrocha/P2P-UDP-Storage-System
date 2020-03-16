package com.feup.sdis.model;

import java.util.*;

public class Store {
    private static Store storeInstance;
    final private Hashtable<String, Integer> replCount = new Hashtable<>(); //TODO: Check if this is the best approach
    final private SortedMap<String, BackupFileInfo> backedUpFiles = new TreeMap<>();
    final private Set<String> storedFiles = new HashSet<>();

    private Store(){}

    public static Store instance(){
        if(storeInstance == null){
            storeInstance = new Store();
        }

        return storeInstance;
    }

    public synchronized SortedMap<String, BackupFileInfo> getBackedUpFiles() { return backedUpFiles; }
    public synchronized Hashtable<String, Integer> getReplCount() {
        return replCount;
    }


    public synchronized Set<String> getStoredFiles() {
        return storedFiles;
    }

}
