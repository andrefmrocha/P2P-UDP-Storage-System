package com.feup.sdis.peer;

import com.feup.sdis.actor.Chunk;
import com.feup.sdis.actor.EnhancedChunk;
import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;

public class DataRestoreChannelReceiver extends Receiver {
    @Override
    MessageActor parseMessage(String msg) throws MessageError {
        final String headerMsg = msg.substring(0, msg.indexOf("\n\r"));
        final Header header = Header.parseHeader(headerMsg);
        MessageActor messageActor;

        if (Chunk.type.equals(header.getMessageType())) {
            if (header.getVersion().equals("1.0"))
                messageActor = new Chunk(new Message(header, MessageActor.parseBody(msg)));
            else
                messageActor = new EnhancedChunk(new Message(header, MessageActor.parseBody(msg)));
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
        return Constants.MDR_CHANNEL;
    }
}