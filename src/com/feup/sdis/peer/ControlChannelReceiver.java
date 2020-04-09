package com.feup.sdis.peer;

import com.feup.sdis.actor.*;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;

import java.net.DatagramPacket;

public class ControlChannelReceiver extends Receiver {


    @Override
    public MessageActor parseMessage(DatagramPacket packet) throws MessageError {
        String msg = new String(packet.getData(), 0, packet.getLength());
        final String headerMsg = msg.substring(0, msg.indexOf("\n\r"));
        final Header header = Header.parseHeader(headerMsg);
        MessageActor messageActor;

        switch (header.getMessageType()) {
            case Stored.type:
                messageActor = new Stored(new Message(header));
                break;
            case GetChunk.type:
                if (header.getVersion().equals("1.0"))
                    messageActor = new GetChunk(new Message(header));
                else
                    messageActor = new EnhancedGetChunk(new Message(header));
                break;
            case Delete.type:
                messageActor = new Delete(new Message(header));
                break;
            case Removed.type:
                messageActor = new Removed(new Message(header));
                break;
            case Deleted.type:
                messageActor = new Deleted(new Message(header));
                break;
            case Excess.type:
                messageActor = new Excess(new Message(header));
                break;
            default:
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
        return Constants.MC_CHANNEL;
    }
}
