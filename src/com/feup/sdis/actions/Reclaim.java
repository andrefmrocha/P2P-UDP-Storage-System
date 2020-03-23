package com.feup.sdis.actions;

import com.feup.sdis.actor.GetChunk;
import com.feup.sdis.actor.Removed;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Map;
import java.util.SortedMap;

import static com.feup.sdis.peer.Constants.MAX_GET_CHUNK_TRIES;

public class Reclaim implements Action {
    private final int maxDiskSpace;

    public Reclaim(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }
        this.maxDiskSpace = Integer.parseInt(args[1]);
    }

    @Override
    public void process() {

        int usedSize = 0;
        SortedMap<String, Integer> storedFiles = Store.instance().getStoredFiles();
        for(Map.Entry<String,Integer> entry : storedFiles.entrySet()) {
            // chunks have the same size
            usedSize += entry.getValue();

            if (usedSize > this.maxDiskSpace) {
                // Send removed message
                try {
                    final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT); //TODO: Changes this to MDR Channel
                    final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
                    socket.joinGroup(group);
                    socket.setTimeToLive(Constants.MC_TTL);
                    socket.setSoTimeout(Constants.MC_TIMEOUT);

                    final String fileID = entry.getKey(); // is this correct?
                    final String senderId = Constants.SENDER_ID;

                    final Header header = new Header(Constants.version, Removed.type, senderId, fileID, i); // get chunk no somehow. from file or store info on putchunk
                    final Message message = new Message(header);
                    final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);

                    socket.send(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
