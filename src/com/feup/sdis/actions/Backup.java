package com.feup.sdis.actions;

import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT); //TODO: Changes this to PUTCHUNK Channel
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            socket.joinGroup(group);
            socket.setTimeToLive(Constants.MC_TTL);
            socket.setSoTimeout(Constants.MC_TIMEOUT);

            final String fileContent = new String(Files.readAllBytes(sendingFile.toPath()), StandardCharsets.UTF_8);
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) this.BLOCK_SIZE );
            final String fileId = Action.generateId(fileContent);
            final String senderId = Constants.SENDER_ID;
            for(int i = 0; i < numChunks; i++){
                int chunkNo = i;
                new Thread(() -> {
                    final String chunk = fileContent.substring(
                            this.BLOCK_SIZE * chunkNo, Math.min(this.BLOCK_SIZE * (chunkNo + 1), fileContent.length()));
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

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
