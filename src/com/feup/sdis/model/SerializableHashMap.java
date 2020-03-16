package com.feup.sdis.model;

import java.io.*;
import java.util.Hashtable;

public class SerializableHashMap {

    private ObjectOutputStream objectOutputStream;
    private Hashtable<String, Integer> files = new Hashtable<>();

    SerializableHashMap(String filename) {
        final File hashFile = new File(filename);
        try {
            if (!hashFile.exists())
                hashFile.createNewFile();
            else {
                final FileInputStream file = new FileInputStream(filename);
                final ObjectInputStream inputStream = new ObjectInputStream(file);
                this.files = (Hashtable<String, Integer>) inputStream.readObject();
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

    public synchronized Integer put(String s, Integer integer) {
        final Integer returnValue = files.put(s, integer);
        this.updateObject();
        return returnValue;
    }

    public synchronized Integer getOrDefault(String s, Integer integer) {
        return files.getOrDefault(s, integer);
    }

    public synchronized Integer get(String s) {
        return files.get(s);
    }

    public synchronized Integer remove(String s) {
        final Integer returnValue = files.remove(s);
        this.updateObject();
        return returnValue;
    }
}
