package com.feup.sdis.actions;

import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;
import static com.feup.sdis.peer.Constants.backupFolder;

public class Backup implements Action {
    private final File sendingFile;
    private final int replDeg;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);


    public Backup(String[] args) throws MessageError {
        if(args.length != 3){
            throw new MessageError("Wrong number of parameters!");
        }

        sendingFile = new File(args[1]);
        replDeg = Integer.parseInt(args[2]);
    }

    public static String sendPutChunk(Path filePath, int replDeg, ScheduledExecutorService scheduler) throws IOException {
        final InetAddress group = InetAddress.getByName(Constants.MDB_CHANNEL);
        final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
        final String fileContent = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
        final int numChunks = (int) Math.ceil(fileContent.length() / (double) BLOCK_SIZE );
        final String fileId = Action.generateId(fileContent);
        final String senderId = Constants.SENDER_ID;
        for(int i = 0; i < numChunks; i++){
            final String chunk = fileContent.substring(
                    BLOCK_SIZE * i, Math.min(BLOCK_SIZE * (i + 1), fileContent.length()));
            final Header header = new Header(Constants.version, PutChunk.type, senderId, fileId, i, replDeg);
            final Message message = new Message(header, chunk);
            final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
            final String chunkId =  message.getHeader().getChunkId();
            AtomicInteger tries = new AtomicInteger();
            Store.instance().getReplCount().put(chunkId, new HashSet<>());
            int chunkNo = i;
            scheduler.scheduleAtFixedRate(()-> {
                if(Store.instance().getReplCount().getOrDefault(chunkId, new HashSet<>()).size() >= replDeg) {
                    System.out.println("Desired replication degree for chunk " + chunkNo + " achieved");
                    throw new RuntimeException();
                }

                if(tries.get() >= Constants.MAX_PUT_CHUNK_TRIES) {
                    System.out.println("Max attempts for PUT_CHUNK of chunk " + chunkNo + " achieved");
                    throw new RuntimeException();
                }

                tries.getAndIncrement();
                try {
                    System.out.println("Sending PUT_CHUNK for chunk " + chunkNo + ", attempt " + (tries.get()+1) + "/" + Constants.MAX_PUT_CHUNK_TRIES);
                    socket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }, 0, 1, TimeUnit.SECONDS);
        }
        return fileContent;
    }

    @Override
    public String process() {
        if(!sendingFile.exists()){
            return "Failed to find file!";
        }
        try {
            final String fileContent = Backup.sendPutChunk(sendingFile.toPath(), this.replDeg, scheduler);
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) BLOCK_SIZE );
            final String fileId = Action.generateId(fileContent);
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