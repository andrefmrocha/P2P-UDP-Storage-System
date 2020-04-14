package com.feup.sdis.actions;

import com.feup.sdis.actor.Deleted;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import com.feup.sdis.peer.Peer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;

public class Delete implements Action {
    private final File file;
    private static final Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20);

    public Delete(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }

        file = new File(args[1]);
        if(!file.exists()) return;
    }

    @Override
    public String process() {
        if(!file.exists()) {
            System.out.println("File does not exist");
            return "File does not exist";
        }

        if (Store.instance().getBackedUpFiles().get(Action.generateId(file)) == null) {
            System.out.println("File was not backed up through this peer");
            return "File was not backed up through this peer";
        }

        try {
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
            final byte[] fileContent = Files.readAllBytes(file.toPath());
            final String fileID = Action.generateId(fileContent, file.lastModified());
            final int numChunks = (int) Math.ceil(fileContent.length / (double) BLOCK_SIZE);

            // remove store info
            int desiredReplDegree = Store.instance().getBackedUpFiles().get(fileID).getDesiredReplicationDegree();
            Store.instance().getBackedUpFiles().remove(fileID);
            if (!Peer.enhanced) {
                for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
                    Store.instance().getReplCount().removeChunkInfo(fileID + Constants.idSeparation + chunkNo);
                }
            }

            final String protocolVersion = Peer.enhanced ? Constants.enhancedVersion : Constants.version;
            final Header header = new Header(
                    protocolVersion,
                    com.feup.sdis.actor.Delete.type,
                    Constants.SENDER_ID, fileID);

            final Message msg = new Message(header);

            // remove stored chunks if exist
            deleteFileChunks(fileID, desiredReplDegree, protocolVersion, scheduler);

            final AtomicInteger tries = new AtomicInteger();
            scheduler.scheduleAtFixedRate(() -> {
                if (Peer.enhanced) {
                    if (tries.get() >= Constants.MAX_DELETE_TRIES) {
                        System.out.println("Maximum DELETE tries achieved, file " + fileID + " couldn't be completely removed");
                        throw new RuntimeException();
                    }
                    if (this.checkReplications(fileID, numChunks)) {
                        System.out.println("File " + fileID + " successfully deleted");
                        throw new RuntimeException();
                    }
                }

                if (!Peer.enhanced && tries.get() == 1)
                    throw new RuntimeException();

                try {
                    System.out.println("Sending DELETE message for file " + fileID + ", attempt " + (tries.get()+1) + "/" + Constants.MAX_DELETE_TRIES);
                    socket.send(msg.generatePacket(group, Constants.MC_PORT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tries.getAndIncrement();

            }, 0, Constants.DELETE_INTERVAL, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Successfully requested DELETE";
    }

    private boolean checkReplications(String fileID, int numChunks) {
        boolean allDeleted = true;
        for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
            if (Store.instance().getReplCount()
                    .getSize(fileID + Constants.idSeparation + chunkNo) > 0) {
                allDeleted = false;
                break;
            }
        }

        return allDeleted;
    }

    public static void deleteFileChunks(String fileID, int desiredReplDegree, String protocolVersion, ScheduledExecutorService scheduler) {
        final SerializableHashMap<StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();
        List<String> storedFilesToRemove = new ArrayList<>();
        for(Map.Entry<String, StoredChunkInfo> entry : storedFiles.entrySet()) {
            String chunkId = entry.getKey();
            if(chunkId.startsWith(fileID)) {
                storedFilesToRemove.add(chunkId);
                System.out.println("Deleting chunk " + chunkId);

                final File file = new File(Constants.backupFolder + chunkId);
                if(!file.delete()){
                    System.out.println("Failed to delete chunk " + chunkId);
                }
                if(protocolVersion.equals(Constants.enhancedVersion)){
                    final Header sendingHeader = new Header(
                            Constants.enhancedVersion,
                            Deleted.type, Constants.SENDER_ID,
                            fileID, Integer.parseInt(chunkId.substring(chunkId.indexOf("#") + 1)),
                            desiredReplDegree);

                    final Message msg = new Message(sendingHeader);
                    scheduler.schedule(() -> {
                        try {
                            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
                            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
                            final DatagramPacket datagramPacket = msg.generatePacket(group, Constants.MC_PORT);
                            socket.send(datagramPacket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }, random.nextInt(400 + 1), TimeUnit.MILLISECONDS);
                    Store.instance().getReplCount().removeID(chunkId, Constants.SENDER_ID);
                }
                else
                    Store.instance().getReplCount().removeChunkInfo(chunkId);
            }
        }
        for (String chunkId : storedFilesToRemove) {
            storedFiles.remove(chunkId);
        }
    }
}
