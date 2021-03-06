package com.feup.sdis.actions;

import com.feup.sdis.actor.Removed;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;

public class Reclaim implements Action {

    public Reclaim(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }
        Store.instance().setMaxDiskSpace(Integer.parseInt(args[1]) * 1000); // kb to bytes
    }

    @Override
    public String process() {
        System.out.println("Starting reclaim protocol");
        int maxDiskSpace = Store.instance().getMaxDiskSpace();
        int usedSize = 0;

        // sort by chunk size
        SerializableHashMap<StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();
        List<Map.Entry<String, StoredChunkInfo>> list = new LinkedList<>(storedFiles.entrySet());
        list.sort(Comparator.comparingInt(t -> t.getValue().getChunkSize()));

        List<String> storedFilesToRemove = new ArrayList<>();
        for(Map.Entry<String,StoredChunkInfo> entry : list) {
            final String chunkID = entry.getKey();
            StoredChunkInfo chunkInfo = entry.getValue();

            usedSize += chunkInfo.getChunkSize();
            if (usedSize > maxDiskSpace) {
                // Must remove
                try {
                    final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT);
                    final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
                    socket.joinGroup(group);
                    socket.setTimeToLive(Constants.MC_TTL);
                    socket.setSoTimeout(Constants.MC_TIMEOUT);

                    final String fileID = chunkInfo.getFileID();
                    final String senderId = Constants.SENDER_ID;
                    final int chunkNo = chunkInfo.getChunkNo();
                    System.out.println("Reached max disk space, sending REMOVED msg for file " + chunkID);

                    // Send Removed message
                    final Header header = new Header(Constants.version, Removed.type, senderId, fileID, chunkNo);
                    final Message message = new Message(header);
                    final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
                    socket.send(datagramPacket);

                    // Delete stored chunk
                    final File file = new File(Constants.backupFolder + chunkID);
                    if(!file.delete()){
                        System.out.println("Failed to delete chunk " + chunkID);
                    }

                    storedFilesToRemove.add(chunkID);
                    Store.instance().getReplCount().removeID(chunkID, Constants.SENDER_ID);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (String chunkId : storedFilesToRemove) {
            storedFiles.remove(chunkId);
        }
        return "Reclaimed space";
    }
}
