package com.feup.sdis.actions;

import com.feup.sdis.actor.Removed;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.SortedMap;

public class Reclaim implements Action {

    public Reclaim(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }
        Store.instance().setMaxDiskSpace(Integer.parseInt(args[1]));
    }

    @Override
    public String process() {
        System.out.println("Starting reclaim protocol");
        int maxDiskSpace = Store.instance().getMaxDiskSpace();
        int usedSize = 0;

        SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();
        for(Map.Entry<String,StoredChunkInfo> entry : storedFiles.entrySet()) {
            final String chunkID = entry.getKey();
            StoredChunkInfo chunkInfo = entry.getValue();
            // chunks have the same size
            usedSize += chunkInfo.getChunkSize();

            if (usedSize > maxDiskSpace) {
                // Must remove
                try {
                    final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT); //TODO: Changes this to MDR Channel
                    final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
                    socket.joinGroup(group);
                    socket.setTimeToLive(Constants.MC_TTL);
                    socket.setSoTimeout(Constants.MC_TIMEOUT);

                    final String fileID = chunkInfo.getFileID();
                    final String senderId = Constants.SENDER_ID;
                    final int chunkNo = chunkInfo.getChunkNo();
                    System.out.println("Reached max disk space, sending REMOVED msg for file " + fileID);

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
                    storedFiles.remove(chunkID);
                    Store.instance().updateReplCount(chunkID, -1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return "Reclaimed space";
    }
}
