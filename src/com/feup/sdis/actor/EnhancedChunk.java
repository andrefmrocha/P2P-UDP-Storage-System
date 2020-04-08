package com.feup.sdis.actor;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class EnhancedChunk extends Chunk  {
    public EnhancedChunk(Message message) {
        super(message);
    }

    @Override
    public void process() throws IOException {
        final String hostname = message.getHeader().getHostname();
        if (hostname == null){
            return;
        }

        final Socket socket = new Socket(hostname, Constants.TCP_PORT);
        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        BackupFileInfo localInfo = Store.instance().getBackedUpFiles().get(fileID);
        if (this.isFileBackedUp(localInfo)){
            out.println("RDY");
            out.flush();
            if(!this.isChunkRestored(localInfo, chunkNo)){
                StringBuilder builder = new StringBuilder();
                String buffer;
                while ((buffer = in.readLine()) != null){
                    builder.append(buffer);
                }
                out.close();
                this.storeFile(builder.toString(), chunkNo, localInfo);
            }
        } else {
            out.println("N/N"); // Not-needed
            out.flush();
            out.close();
        }
        while (in.readLine() != null);
        in.close();
        socket.close();
    }
}
