package com.feup.sdis.actions;

import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import com.feup.sdis.peer.Peer;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;
import static com.feup.sdis.peer.Constants.MAX_PUT_CHUNK_TRIES;


class Task implements Runnable {
    private int tries;
    private final String chunkId;
    private final int replDeg;
    private final DatagramPacket datagramPacket;
    private final MulticastSocket socket;
    private final ScheduledExecutorService scheduler;
    private final int chunkNo;

    public Task(int tries, String chunkId, int replDeg,
                DatagramPacket datagramPacket, MulticastSocket socket, ScheduledExecutorService scheduler, int chunkNo) {
        this.tries = tries;
        this.chunkId = chunkId;
        this.replDeg = replDeg;
        this.datagramPacket = datagramPacket;
        this.socket = socket;
        this.scheduler = scheduler;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        if (Store.instance().getReplCount().getSize(chunkId) >= replDeg) {
            System.out.println("Desired replication degree for chunk " + chunkNo + " achieved");
            throw new RuntimeException();
        }

        if (tries >= Constants.MAX_PUT_CHUNK_TRIES) {
            System.out.println("Max attempts for PUT_CHUNK of chunk " + chunkNo + " achieved");
            throw new RuntimeException();
        }

        tries++;

        try {
            System.out.println("Sending PUT_CHUNK for chunk " + chunkNo + ", attempt " + tries + "/" + Constants.MAX_PUT_CHUNK_TRIES);

            socket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (tries < Constants.MAX_PUT_CHUNK_TRIES) {
            scheduler.schedule(
                    new Task(tries, chunkId, replDeg, datagramPacket, socket, scheduler, chunkNo),
                    tries == 1 ? 1 : (tries - 1) * 2, TimeUnit.SECONDS);
        }
    }
}

public class Backup implements Action {
    private final File sendingFile;
    private final int replDeg;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);

    public Backup(String[] args) throws MessageError {
        if (args.length != 3) {
            throw new MessageError("Wrong number of parameters!");
        }

        sendingFile = new File(args[1]);
        replDeg = Integer.parseInt(args[2]);
    }

    public static byte[] sendPutChunk(File sendingFile, int replDeg, ScheduledExecutorService scheduler) throws IOException {
        final InetAddress group = InetAddress.getByName(Constants.MDB_CHANNEL);
        final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, group);
        final byte[] fileContent = Files.readAllBytes(sendingFile.toPath());
        final double division = fileContent.length / (double) BLOCK_SIZE;
        final int numChunks = (int) Math.ceil(division);
        final String fileId = Action.generateId(fileContent, sendingFile.lastModified());
        final String senderId = Constants.SENDER_ID;
        for (int i = 0; i < numChunks; i++) {
            final byte[] chunk = Arrays.copyOfRange(fileContent, BLOCK_SIZE * i, Math.min(BLOCK_SIZE * (i + 1), fileContent.length));
            final Header header = new Header(Peer.enhanced ? Constants.enhancedVersion : Constants.version
                    , PutChunk.type, senderId, fileId, i, replDeg);
            final Message message = new Message(header, chunk);
            final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
            final String chunkId = message.getHeader().getChunkId();
            scheduler.schedule(
                    new Task(0, chunkId, replDeg, datagramPacket, socket, scheduler, i), 1, TimeUnit.SECONDS);
        }

        if(division == numChunks){
            System.out.println("File size is multiple of chunk size, sending chunk of size 0");
            final Header header = new Header(Peer.enhanced ? Constants.enhancedVersion : Constants.version
                    , PutChunk.type, senderId, fileId, numChunks, replDeg);
            final Message message = new Message(header, new byte[0]);
            final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
            socket.send(datagramPacket);
        }

        return fileContent;
    }

    @Override
    public String process() {
        if (!sendingFile.exists()) {
            System.out.println("Failed to find file!");
            return "Failed to find file!";
        }
        try {
            final byte[] fileContent = Backup.sendPutChunk(sendingFile, this.replDeg, scheduler);
            final int numChunks = (int) Math.ceil(fileContent.length / (double) BLOCK_SIZE);
            final String fileId = Action.generateId(fileContent, sendingFile.lastModified());
            Store.instance().getBackedUpFiles().put(fileId, new BackupFileInfo(fileId,
                    sendingFile.getName(),
                    sendingFile.getPath(),
                    numChunks,
                    this.replDeg));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Stored file";
    }
}