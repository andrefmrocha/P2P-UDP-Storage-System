package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SerializableHashMap {

    private String filename;
    private Map<String, Set<String>> files = new ConcurrentHashMap<>();

    SerializableHashMap(String filename) {
        this.filename = filename;
        final File hashFile = new File(filename);
        try {
            if (!hashFile.exists())
                hashFile.createNewFile();
            else {
                final FileInputStream file = new FileInputStream(filename);
                final ObjectInputStream inputStream = new ObjectInputStream(file);
                this.files = (ConcurrentHashMap<String, Set<String>>) inputStream.readObject();
                inputStream.close();
                file.close();
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            updateObject();
        }
    }

    private synchronized void updateObject(){
        try {
            final FileOutputStream outputStream = new FileOutputStream(filename);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(files);
            objectOutputStream.flush();
            objectOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized Set<String> getOrDefault(String s, Set<String> set) {
        return files.getOrDefault(s, Collections.synchronizedSet(set));
    }

    private synchronized Set<String> remove(String s) {
        final Set<String> returnValue = files.remove(s);
        this.updateObject();
        return returnValue;
    }

    public synchronized int getSize(String key){
        return this.getOrDefault(key, new HashSet<>()).size();
    }

    public synchronized void removeChunkInfo(String key){
        this.remove(key);
    }

    public synchronized void addNewID(String key, String peerId){
        Set<String> peers = this.getOrDefault(key, new HashSet<>());
        peers.add(peerId);
        this.files.put(key, peers);
        this.updateObject();
    }

    public synchronized void removeID(String key, String peerId){
        Set<String> peers = this.getOrDefault(key, new HashSet<>());
        peers.remove(peerId);
        this.files.put(key, peers);
        this.updateObject();
    }

    public synchronized boolean contains(String key){
        return this.files.containsKey(key);
    }

    public synchronized boolean containsPeer(String key, String peerId){
        return this.getOrDefault(key, new HashSet<>()).contains(peerId);
    }
}
