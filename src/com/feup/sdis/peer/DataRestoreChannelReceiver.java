package com.feup.sdis.peer;

import com.feup.sdis.actor.Chunk;
import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;

import java.net.DatagramPacket;
import java.util.Arrays;

public class DataRestoreChannelReceiver extends Receiver {
    @Override
    MessageActor parseMessage(DatagramPacket packet) throws MessageError {
        final String msg = new String(packet.getData(), 0, packet.getLength());
        byte[] msgBytes = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());

        final String headerMsg = msg.substring(0, msg.indexOf("\r\n"));
        final Header header = Header.parseHeader(headerMsg);
        MessageActor messageActor;

        if (Chunk.type.equals(header.getMessageType())) {
            messageActor = new Chunk(new Message(header, MessageActor.parseBody(msg, msgBytes)));
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
