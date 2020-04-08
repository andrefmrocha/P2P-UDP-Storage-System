package com.feup.sdis.actions;

import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import com.feup.sdis.peer.Peer;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;

public class Delete implements Action {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private final File file;

    public Delete(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }
        this.file = new File(args[1]);
    }

    @Override
    public String process() {
        System.out.println("Starting delete protocol");
        if (!file.exists()) {
            return "Failed to find file!";
        }

        try {
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
            final String fileContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            final String fileID = Action.generateId(fileContent, file.lastModified());
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) BLOCK_SIZE);

            // remove store info
            Store.instance().getBackedUpFiles().remove(fileID);
            if (!Peer.enhanced) {
                for (int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
                    Store.instance().getReplCount().removeChunkInfo(fileID + Constants.idSeparation + chunkNo);
                }
            }

            final Header header = new Header(
                    Peer.enhanced ? Constants.enhancedVersion : Constants.version,
                    com.feup.sdis.actor.Delete.type,
                    Constants.SENDER_ID, fileID);

            final Message msg = new Message(header);

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
                    System.out.println("Sending DELETE message for file " + fileID);
                    socket.send(msg.generatePacket(group, Constants.MC_PORT));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                tries.getAndIncrement();

            }, 0, Constants.DELETE_INTERVAL, TimeUnit.SECONDS);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Deleted file";
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
}
