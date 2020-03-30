package com.feup.sdis.actions;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static com.feup.sdis.peer.Constants.BLOCK_SIZE;

public class Delete implements Action {
    private final File file;

    public Delete(String[] args) throws MessageError {
        if (args.length != 2) {
            throw new MessageError("Wrong number of parameters!");
        }
        this.file = new File(args[1]);
    }

    @Override
    public void process() {
        if (!file.exists()) {
            System.out.println("Failed to find file!");
            return;
        }

        try {
            //TODO: Generalize socket generation
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT);
            final InetAddress group = InetAddress.getByName(Constants.MC_CHANNEL);
            socket.joinGroup(group);
            socket.setTimeToLive(Constants.MC_TTL);
            socket.setSoTimeout(Constants.MC_TIMEOUT);

            final String fileContent = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            final String fileID = Action.generateId(fileContent);
            final int numChunks = (int) Math.ceil(fileContent.length() / (double) BLOCK_SIZE );

            // remove store info
            Store.instance().getBackedUpFiles().remove(fileID);
            for(int chunkNo = 0; chunkNo < numChunks; chunkNo++) {
                Store.instance().getReplCount().remove(fileID + Constants.idSeparation + chunkNo);
            }

            final Header header = new Header(
                    Constants.version,
                    com.feup.sdis.actor.Delete.type,
                    Constants.SENDER_ID, fileID);

            final Message msg = new Message(header);
            socket.send(msg.generatePacket(group, Constants.MC_PORT));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
