package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.util.HashSet;
import java.util.Set;

public class Store {
    private static Store storeInstance;
    final private SerializableHashMap replCount = new SerializableHashMap(Constants.SENDER_ID + "/" + "files.ser");
    //TODO: Check if this is the best approach
    final private Set<String> storedFiles = new HashSet<>();

    private Store(){}

    public static Store instance(){
        if(storeInstance == null){
            storeInstance = new Store();
        }

        return storeInstance;
    }

    public synchronized SerializableHashMap getReplCount() {
        return replCount;
    }


    public synchronized Set<String> getStoredFiles() {
        return storedFiles;
    }
}
