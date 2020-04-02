package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;

public class EnhancedGetChunk extends GetChunk {
    public EnhancedGetChunk(Message message) {
        super(message);
    }

    private static ServerSocket socket;


    @Override
    protected void sendFile(String fileID, String chunkNo, String fileContent) throws IOException {
        final Header sendingHeader = new Header(
                Constants.enhancedVersion,
                Chunk.type, Constants.SENDER_ID,
                fileID, Integer.parseInt(chunkNo), -1,
                InetAddress.getLocalHost().getHostAddress());
        this.sendMessage(Constants.MC_PORT, Constants.MDR_CHANNEL, new Message(sendingHeader));

        if (socket == null)
            socket = new ServerSocket(Constants.TCP_PORT);

        final Socket client = socket.accept();
        final PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        if (in.readLine().equals("RDY")){
            out.println(fileContent);
        }
        out.flush();
        out.close();
        while (in.readLine() != null);
        in.close();
        client.close();
    }
}
