package com.feup.sdis.actions;

import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public class Backup implements Action {
    private final File sendingFile;
    private final int replDeg;
    private final int BLOCK_SIZE = 64000;

    public Backup(String[] args) throws MessageError {
        if(args.length != 3){
            throw new MessageError("Wrong number of parameters!");
        }

        sendingFile = new File(args[1]);
        replDeg = Integer.parseInt(args[2]);
    }

    @Override
    public void process() {
        if(!sendingFile.exists()){
            System.out.println("Failed to find file!");
            return;
        }

        try {
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT);
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            socket.joinGroup(group);
            socket.setTimeToLive(Constants.MC_TTL);
            socket.setSoTimeout(Constants.MC_TIMEOUT);

            final String fileContent = new String(Files.readAllBytes(sendingFile.toPath()), StandardCharsets.UTF_8);
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) this.BLOCK_SIZE );
            final String fileId = Integer.toString((fileContent + Constants.SENDER_ID).hashCode()); // TODO: Check actual algorithm for file storage
            final String senderId = Constants.SENDER_ID;
            for(int i = 0; i < numChunks; i++){
                final String chunk = fileContent.substring(
                        this.BLOCK_SIZE * i, Math.min(this.BLOCK_SIZE * (i + 1), fileContent.length()));
                final Header header = new Header(Constants.version, PutChunk.type, senderId, fileId, i, this.replDeg);
                final Message message = new Message(header, chunk);
                final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
                socket.send(datagramPacket);
                Store.instance().getReplCount().put(fileId + this.replDeg, 0);
                for (int tries = 0; tries < Constants.MAX_PUT_CHUNK_TRIES; tries++){
                    System.out.println("Replication degree not achieved for chunk no " + i + "of file " + fileId);
                    try {
                        Thread.sleep(1000);
                        if(Store.instance().getReplCount().get(fileId + this.replDeg) >= this.replDeg)
                            break;
                        socket.send(datagramPacket);
                    } catch (InterruptedException e) {
                        tries--;
                        e.printStackTrace();
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
