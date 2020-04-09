package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.*;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class EnhancedGetChunk extends GetChunk {
    public EnhancedGetChunk(Message message) {
        super(message);
    }

    private ServerSocket socket;


    public ServerSocket isAvailable(int port) {
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

    @Override
    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        if (socket == null) {
            for (int port = Constants.TCP_PORT; port < Constants.TCP_PORT + 1000; port++) {
                if ((socket = isAvailable(port)) != null) {
                    System.out.println("Found available port in " + port);
                    break;
                }
            }
        }

        if (socket == null) {
            System.out.println("Failed to allocate a port, exiting.");
            return;
        }

        final Header sendingHeader = new Header(
                Constants.enhancedVersion,
                Chunk.type, Constants.SENDER_ID,
                fileID, Integer.parseInt(chunkNo), -1,
                InetAddress.getLocalHost().getHostAddress() + ":" + socket.getLocalPort());
        if (! this.sendGetChunk(Constants.MC_PORT, Constants.MDR_CHANNEL, new Message(sendingHeader)))
            return;

        final Socket client = socket.accept();
        final DataOutputStream out = new DataOutputStream(client.getOutputStream());
        final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        if (in.readLine() == null) {
            out.writeInt(fileContent.length);
            out.write(fileContent);
            out.flush();
        }
        while (!client.isClosed() && in.readLine() != null) ;
        out.close();
        in.close();
        client.close();
    }
}
