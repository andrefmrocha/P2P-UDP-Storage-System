package com.feup.sdis.actor;

import com.feup.sdis.model.BackupFileInfo;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.io.*;
import java.net.Socket;

public class EnhancedChunk extends Chunk  {
    public EnhancedChunk(Message message) {
        super(message);
    }

    @Override
    public void process() throws IOException {
        final String hostname = message.getHeader().getExtraParam();
        if (hostname == null){
            return;
        }

        final Socket socket = new Socket(hostname, Constants.TCP_PORT);
        final PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        final DataInputStream in = new DataInputStream(socket.getInputStream());
        final String fileID = message.getHeader().getFileId();
        final int chunkNo = Integer.parseInt(message.getHeader().getChunkNo());
        BackupFileInfo localInfo = Store.instance().getBackedUpFiles().get(fileID);
        if (this.isFileBackedUp(localInfo)){
            out.println("RDY");
            out.flush();
            if(!this.isChunkRestored(localInfo, chunkNo)){
                final int length = in.readInt();
                if(length > 0){
                    final byte[] message = new byte[length];
                    in.readFully(message, 0, message.length);
                    this.storeFile(message, chunkNo, localInfo);
                }
                out.close();
            }
        } else {
            out.println("N/N"); // Not-needed
            out.flush();
            out.close();
        }
        while (!socket.isClosed() && in.read() != -1);
        in.close();
        socket.close();
    }
}
