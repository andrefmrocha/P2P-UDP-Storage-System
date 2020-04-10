package com.feup.sdis.actor;

import com.feup.sdis.model.Message;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class EnhancedGetChunk extends GetChunk {
    public EnhancedGetChunk(Message message) {
        super(message);
    }

    @Override
    protected void sendFile(String fileID, String chunkNo, byte[] fileContent) throws IOException {
        final String extraParam = message.getHeader().getExtraParam();
        final String[] splitted = extraParam.split(":");
        if (splitted.length != 2) {
            System.out.println("hostname:port not successfully found, exiting");
            return;
        }
        final String hostname = splitted[0];
        final int port = Integer.parseInt(splitted[1]);

        final Socket client = new Socket(hostname, port);
        final DataOutputStream out = new DataOutputStream(client.getOutputStream());
        final BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        if (in.readLine() == null) {
            out.writeInt(fileContent.length);
            out.write(fileContent);
            out.flush();
            client.shutdownOutput();
        }
        while (in.readLine() != null) ;
        client.shutdownInput();
        client.close();
    }
}
