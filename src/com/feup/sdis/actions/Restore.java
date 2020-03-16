package com.feup.sdis.actions;

import com.feup.sdis.actor.GetChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

import static com.feup.sdis.peer.Constants.MAX_GET_CHUNK_TRIES;

public class Restore implements Action {
    private BackupFileInfo backupFileInfo;
    private final Random random = new Random();

    public Restore(String[] args) throws MessageError {
        if(args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }

        for (BackupFileInfo f : Store.instance().getBackedUpFiles().values()){
            if (f.getOriginalPath().equals(args[1])) {
                backupFileInfo = f;
                break;
            }
        }
    }

    @Override
    public void process() {
        if (backupFileInfo == null) {
            System.out.println("File is not backed up!");
            return;
        }

        backupFileInfo.getRestoredChunks().clear();

        try {
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT); //TODO: Changes this to MDR Channel
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            socket.joinGroup(group);
            socket.setTimeToLive(Constants.MC_TTL);
            socket.setSoTimeout(Constants.MC_TIMEOUT);

            final String fileID = backupFileInfo.getfileID();
            final int numChunks = backupFileInfo.getNChunks();
            final String senderId = Constants.SENDER_ID;
            for(int i = 0; i < numChunks; i++){
                System.out.println(numChunks);

                final Header header = new Header(Constants.version, GetChunk.type, senderId, fileID, i);
                final Message message = new Message(header);
                final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);

                int tries = MAX_GET_CHUNK_TRIES;
                int chunkN = i;
                new Thread(()-> {
                    try {
                        for (int t = 0; t < tries; t++) {

                            if(backupFileInfo.getRestoredChunks().contains(chunkN)) break;

                            Thread.sleep(random.nextInt(400 + 1));
                            socket.send(datagramPacket);
                        }

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
