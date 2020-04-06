package com.feup.sdis.actor;

import com.feup.sdis.model.*;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Random;
import java.util.SortedMap;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;

public class Removed extends MessageActor {
    final static public String type =  "REMOVED";
    private final Random random = new Random();

    public Removed(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId = message.getHeader().getChunkId();
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        final SortedMap<String, StoredChunkInfo> storedFiles = Store.instance().getStoredFiles();

        // update repl count on all peers
        final SerializableHashMap replCount = Store.instance().getReplCount();
        Store.instance().updateReplCount(chunkId, -1);
        int newReplDegree = Store.instance().getReplCount().get(chunkId);

        if(!storedFiles.containsKey(chunkId)) return;

        final StoredChunkInfo stored = storedFiles.get(chunkId);
        if (newReplDegree < stored.getDesiredReplicationDegree()) {
            System.out.println("Replication degree dropped below the desired level");
            System.out.println("Starting backup protocol for chunk " + chunkId);
            int replDeg = stored.getDesiredReplicationDegree();

            try {
                final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT); //TODO: Changes this to PUTCHUNK Channel
                final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
                socket.joinGroup(group);
                socket.setTimeToLive(Constants.MC_TTL);
                socket.setSoTimeout(Constants.MC_TIMEOUT);

                File chunkFile = new File(Constants.backupFolder + chunkId);
                final String fileContent = new String(Files.readAllBytes(chunkFile.toPath()), StandardCharsets.UTF_8);
                final Header header = new Header(Constants.version, PutChunk.type, Constants.SENDER_ID, fileID, chunkNo, replDeg);
                final Message message = new Message(header, fileContent);
                final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);

                new Thread(() -> {
                    for (int tries = 0; tries < Constants.MAX_PUT_CHUNK_TRIES; tries++){
                        try {
                            Thread.sleep(random.nextInt(400 + 1));
                            if(replCount.get(chunkId) >= replDeg) {
                                break;
                            }
                            System.out.println("Sending PUT_CHUNK for chunk " + chunkId + ", attempt " + (tries+1) + "/" + Constants.MAX_PUT_CHUNK_TRIES);
                            socket.send(datagramPacket);
                        } catch (InterruptedException | IOException e) {
                            tries--;
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
