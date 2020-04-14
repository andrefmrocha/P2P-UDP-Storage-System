package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SocketFactory;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class GetChunk extends MessageActor {
    final static public String type = "GETCHUNK";

    public GetChunk(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() throws IOException {
        final String fileID = message.getHeader().getFileId();
        final String chunkNo = message.getHeader().getChunkNo();
        final String chunkId = message.getHeader().getChunkId();

        if (!Store.instance().getStoredFiles().containsKey(chunkId)) {
            System.out.println("Do not have chunk " + chunkId + " stored");
            return;
        }
        File chunkFile = new File(Constants.backupFolder + chunkId);
        final byte[] fileContent = Files.readAllBytes(chunkFile.toPath());
        System.out.println("Sending CHUNK msg for chunk " + chunkId);
        sendFile(fileID, chunkNo, fileContent);
    }

    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        final Header sendingHeader = new Header(
                Constants.version,
                Chunk.type, Constants.SENDER_ID,
                fileID, Integer.parseInt(chunkNo));

        final Message msg = new Message(sendingHeader, fileContent);
        this.sendGetChunk(Constants.MDR_PORT, Constants.MDR_CHANNEL, msg);
    }

    protected boolean sendGetChunk(int port, String groupChannel, Message msg) {
        try {
            return scheduler.schedule(() -> {
                try {
                    final String chunkId = message.getHeader().getChunkId();
                    if (Store.instance().getChunksSent().contains(chunkId)) {
                        System.out.println("Chunk " + chunkId + " has already been sent");
                        return false;
                    }

                    final InetAddress group = InetAddress.getByName(groupChannel);
                    final MulticastSocket socket = SocketFactory.buildMulticastSocket(port, group);
                    final DatagramPacket datagramPacket = msg.generatePacket(group, Constants.MDR_PORT);
                    socket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
                return true;

            }, random.nextInt(400 + 1), TimeUnit.MILLISECONDS).get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
