package com.feup.sdis.actions;

import com.feup.sdis.actor.GetChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import com.feup.sdis.peer.Peer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feup.sdis.peer.Constants.MAX_GET_CHUNK_TRIES;

public class Restore implements Action {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);
    private BackupFileInfo backupFileInfo;

    public Restore(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }

        for (BackupFileInfo f : Store.instance().getBackedUpFiles().values()) {
            if (f.getOriginalPath().equals(args[1])) {
                backupFileInfo = f;
                break;
            }
        }
    }

    @Override
    public String process() {
        if (backupFileInfo == null) {
            return "File is not backed up!";
        }

        backupFileInfo.getRestoredChunks().clear();

        try {
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, Constants.MC_CHANNEL);

            final String fileID = backupFileInfo.getfileID();
            final int numChunks = backupFileInfo.getNChunks();
            final String senderId = Constants.SENDER_ID;
            for (int i = 0; i < numChunks; i++) {
                System.out.println(numChunks);

                final Header header = new Header(Peer.enhanced ? Constants.enhancedVersion : Constants.version,
                                    GetChunk.type, senderId, fileID, i);
                final Message message = new Message(header);
                final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);

                final int chunkN = i;
                final AtomicInteger tries = new AtomicInteger();
                scheduler.scheduleAtFixedRate(() -> {
                    if (backupFileInfo.getRestoredChunks().contains(chunkN) || backupFileInfo.isFullyRestored() ||
                        tries.get() >= MAX_GET_CHUNK_TRIES)
                        throw new RuntimeException();

                    tries.getAndIncrement();
                    try {
                        socket.send(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 1, 1, TimeUnit.SECONDS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Restored " + backupFileInfo.getOriginalFilename();
    }
}
