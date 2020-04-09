package com.feup.sdis.model;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SerializableHashMap {

    private ObjectOutputStream objectOutputStream;
    private Map<String, Set<String>> files = new ConcurrentHashMap<>();

    SerializableHashMap(String filename) {
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
            try {
                final FileOutputStream outputStream = new FileOutputStream(filename);
                this.objectOutputStream = new ObjectOutputStream(outputStream);
                objectOutputStream.writeObject(files);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void updateObject(){
        try {
            objectOutputStream.writeObject(files);
            objectOutputStream.flush();
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
