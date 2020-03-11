package com.feup.sdis.model;

import com.feup.sdis.actor.*;

public class Message {
    private final Header header;
    private final byte[] body;

    public Message(Header header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public Message(Header header) {
        this(header, null);
    }

    public static MessageActor parseMessage(String msg) throws MessageError {
        final String headerMsg = msg.substring(0, msg.indexOf("\n\n"));
        final Header header = Header.parseHeader(headerMsg);
        MessageActor messageActor;

        switch (header.getMessageType()) {
            case PutChunk.type:
                messageActor = new PutChunk(new Message(header, MessageActor.parseBody(msg)));
                break;
            case Stored.type:
                messageActor = new Stored(new Message(header));
                break;
            case GetChunk.type:
                messageActor = new GetChunk(new Message(header));
                break;
            case Chunk.type:
                messageActor = new Chunk(new Message(header, MessageActor.parseBody(msg)));
                break;
            case Delete.type:
                messageActor = new Delete(new Message(header));
                break;
            case Removed.type:
                messageActor = new Removed(new Message(header));
                break;
            default:
                throw new MessageError("Unexpected message type: " + header.getMessageType());
        }
        return messageActor;
    }

    public byte[] getBody() {
        return body;
    }

    public Header getHeader() {
        return header;
    }
}
