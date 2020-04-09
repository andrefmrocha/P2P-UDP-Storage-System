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

    private static ServerSocket socket;


    public static boolean isAvailable(int port) {
        ServerSocket server = null;
        DatagramSocket datagramSocket = null;
        try {
            server = new ServerSocket(port);
            server.setReuseAddress(true);
            datagramSocket = new DatagramSocket(port);
            datagramSocket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }

            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    /* should not be thrown */
                }
            }
        }

        return false;
    }

    @Override
    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        int port = -1;
        if (socket == null) {
            for (port = Constants.TCP_PORT; port < Constants.TCP_PORT + 1000; port++) {
                if (isAvailable(port)) {
                    System.out.println("Found available port in " + port);
                    socket = new ServerSocket(port);
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
        this.sendMessage(Constants.MC_PORT, Constants.MDR_CHANNEL, new Message(sendingHeader));


        final Socket client = socket.accept();
        final DataOutputStream out = new DataOutputStream(client.getOutputStream());
        final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        if (in.readLine().equals("RDY")) {
            out.writeInt(fileContent.length);
            out.write(fileContent);
        }
        out.flush();
        out.close();
        while (!client.isClosed() && in.readLine() != null) ;
        in.close();
        client.close();
    }
}
