package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.actor.PutChunk;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.SocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class DataBackupChannelReceiver extends Receiver {
    @Override
    public MessageActor parseMessage(String msg) throws MessageError {
        final String headerMsg = msg.substring(0, msg.indexOf("\n\r"));
        final Header header = Header.parseHeader(headerMsg);
        MessageActor messageActor;

        if (PutChunk.type.equals(header.getMessageType())) {
            messageActor = new PutChunk(new Message(header, MessageActor.parseBody(msg)));
        } else {
            throw new MessageError("Unexpected message type: " + header.getMessageType());
        }
        return messageActor;
    }

    @Override
    int getPort() {
        return Constants.MC_PORT;
    }

    @Override
    String getChannel() {
        return Constants.MDB_CHANNEL;
    }
}
