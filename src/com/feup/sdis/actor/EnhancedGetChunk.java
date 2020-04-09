package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EnhancedGetChunk extends GetChunk {
    public EnhancedGetChunk(Message message) {
        super(message);
    }

    private static ServerSocket socket;


    @Override
    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        final Header sendingHeader = new Header(
                Constants.enhancedVersion,
                Chunk.type, Constants.SENDER_ID,
                fileID, Integer.parseInt(chunkNo), -1,
                InetAddress.getLocalHost().getHostAddress());
        this.sendMessage(Constants.MC_PORT, Constants.MDR_CHANNEL, new Message(sendingHeader));

        if (socket == null)
            socket = new ServerSocket(Constants.TCP_PORT);

        final Socket client = socket.accept();
        final DataOutputStream out = new DataOutputStream(client.getOutputStream());
        final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        if (in.readLine().equals("RDY")){
            out.writeInt(fileContent.length);
            out.write(fileContent);
        }
        out.flush();
        out.close();
        while (!client.isClosed() && in.readLine() != null);
        in.close();
        client.close();
    }
}
