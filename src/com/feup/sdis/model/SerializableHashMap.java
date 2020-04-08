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
            }

            final FileOutputStream outputStream = new FileOutputStream(filename);
            this.objectOutputStream = new ObjectOutputStream(outputStream);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private synchronized void updateObject(){
        try {
            objectOutputStream.writeObject(files);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized Set<String> put(String s, Set<String> integer) {
        final Set<String> returnValue = files.put(s, integer);
        this.updateObject();
        return returnValue;
    }

    public synchronized Set<String> getOrDefault(String s, Set<String> set) {
        return files.getOrDefault(s, Collections.synchronizedSet(set));
    }

    public synchronized Set<String> get(String s) {
        return files.get(s);
    }

    public synchronized Set<String> remove(String s) {
        final Set<String> returnValue = files.remove(s);
        this.updateObject();
        return returnValue;
    }
}
