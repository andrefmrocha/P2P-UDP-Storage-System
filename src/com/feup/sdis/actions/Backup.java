package com.feup.sdis.actions;

import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;

public class Backup implements Action {
    private final File sendingFile;
    private final int replDeg;

    public Backup(String[] args) throws MessageError {
        if(args.length != 3){
            throw new MessageError("Wrong number of parameters!");
        }

        sendingFile = new File(args[1]);
        replDeg = Integer.parseInt(args[2]);
    }

    @Override
    public String process() {
        if(!sendingFile.exists()){
            return "Failed to find file!";
        }

        try {
            final InetAddress group = InetAddress.getByName(Constants.MDB_CHANNEL);
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
            final String fileContent = new String(Files.readAllBytes(sendingFile.toPath()), StandardCharsets.UTF_8);
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) BLOCK_SIZE );
            final String fileId = Action.generateId(fileContent);
            final String senderId = Constants.SENDER_ID;
            for(int i = 0; i < numChunks; i++){
                int chunkNo = i;
                new Thread(() -> {
                    final String chunk = fileContent.substring(
                            BLOCK_SIZE * chunkNo, Math.min(BLOCK_SIZE * (chunkNo + 1), fileContent.length()));
                    final Header header = new Header(Constants.version, PutChunk.type, senderId, fileId, chunkNo, this.replDeg);
                    final Message message = new Message(header, chunk);
                    final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
                    final String chunkId =  message.getHeader().getChunkId();
                    try {
                        socket.send(datagramPacket);
                        Store.instance().getReplCount().put(chunkId, 0);
                        for (int tries = 0; tries < Constants.MAX_PUT_CHUNK_TRIES; tries++){
                            try {
                                Thread.sleep(1000);
                                if(Store.instance().getReplCount().get(chunkId) >= this.replDeg) {
                                    break;
                                }
                                System.out.println("Replication degree not achieved for chunk no " + chunkNo + " of file " + fileId);
                                socket.send(datagramPacket);
                            } catch (InterruptedException e) {
                                tries--;
                                e.printStackTrace();
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
            Store.instance().getBackedUpFiles().put(fileId, new BackupFileInfo( fileId,
                                                                                sendingFile.getName(),
                                                                                sendingFile.getPath(),
                                                                                numChunks));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Stored file";
    }
}
