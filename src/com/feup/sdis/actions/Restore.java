package com.feup.sdis.actions;

import com.feup.sdis.actor.GetChunk;
import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;
import com.feup.sdis.peer.Peer;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.feup.sdis.actor.Chunk.*;
import static com.feup.sdis.peer.Constants.MAX_GET_CHUNK_TRIES;

public class Restore implements Action {
    private final File file;
    private BackupFileInfo backupFileInfo;

    public Restore(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }

        file = new File(args[1]);
        if (!file.exists()) return;

        String fileID = Action.generateId(file);
        backupFileInfo = Store.instance().getBackedUpFiles().get(fileID);
    }

    @Override
    public String process() {
        if (!file.exists()) {
            System.out.println("File does not exist");
            return "File does not exist";
        }

        if (backupFileInfo == null) {
            System.out.println("File was not backed up through this peer");
            return "File was not backed up through this peer";
        }

        backupFileInfo.getRestoredChunks().clear();

        try {
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(Constants.MC_PORT, Constants.MC_CHANNEL);

            final String fileID = backupFileInfo.getfileID();
            final int numChunks = backupFileInfo.getNChunks();
            final String senderId = Constants.SENDER_ID;
            System.out.println("Starting restore protocol for file " + fileID);
            for (int i = 0; i < numChunks; i++) {

                final Header header = Peer.enhanced ? prepareEnhancedRestored(fileID, senderId, i)
                        : new Header(Constants.version, GetChunk.type, senderId, fileID, i);
                final Message message = new Message(header);
                final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);

                final int chunkN = i;
                final AtomicInteger tries = new AtomicInteger();
                scheduler.scheduleAtFixedRate(() -> {
                    if (backupFileInfo.getRestoredChunks().get(chunkN) != null || backupFileInfo.isFullyRestored() ||
                            tries.get() >= MAX_GET_CHUNK_TRIES)
                        throw new RuntimeException();

                    tries.getAndIncrement();
                    try {
                        System.out.println("Sending GET_CHUNK for chunk " + chunkN);
                        socket.send(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }, 1, 3, TimeUnit.SECONDS);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Restored " + backupFileInfo.getOriginalFilename();
    }


    private ServerSocket isAvailable(int port) {
        ServerSocket server = null;
        DatagramSocket datagramSocket = null;
        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setReuseAddress(true);
        } catch (IOException e) {
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

        return server;
    }


    private Header prepareEnhancedRestored(String fileID, String senderId, int chunkNo) {
        ServerSocket socket = null;
        for (int port = Constants.TCP_PORT; port < Constants.TCP_PORT + 1000; port++) {
            if ((socket = isAvailable(port)) != null) {
                System.out.println("Found available port in " + port);
                break;
            }
        }

        if (socket == null) {
            System.out.println("Failed to allocate a port, exiting.");
            return null;
        }

        ServerSocket finalSocket = socket;
        scheduler.execute(() -> {
            try {
                final Socket client = finalSocket.accept();
                if (!isFileBackedUp(backupFileInfo)) {
                    Store.instance().getChunksSent().add(fileID + Constants.idSeparation + chunkNo);
                    System.out.println("This peer was not the Restore initiator");
                    return;
                }
                final PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                final DataInputStream in = new DataInputStream(client.getInputStream());

                if (!initialChecks(chunkNo, backupFileInfo)) {
                    client.shutdownOutput();
                    final int length = in.readInt();
                    if (length > 0) {
                        final byte[] message = new byte[length];
                        in.readFully(message, 0, message.length);
                        storeFile(message, chunkNo, backupFileInfo);
                    } else {
                        System.out.println("Read length " + length);
                    }

                } else {
                    out.println("N/N"); // Not-needed
                    out.flush();
                }
                while (in.read() != -1) ;
                client.shutdownInput();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        try {
            return new Header(Constants.enhancedVersion,
                    GetChunk.type, senderId, fileID, chunkNo, -1,
                    InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
