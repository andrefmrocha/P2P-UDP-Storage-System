package com.feup.sdis.model;

import com.feup.sdis.actor.*;

import java.util.Arrays;
import java.util.UUID;

public class Header {
    private char[] version;
    private MessageType messageType;
    private UUID senderId;
    private UUID fileId;
    private char[] chunkNo;
    private int replicationDeg;

    public Header(char[] version, MessageType messageType, UUID senderId, UUID fileId, int chunkNo, int replicationDeg) {
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        final String chunkString = Integer.toString(chunkNo);
        if (version.length != 3 || chunkString.length() <= 6 || chunkNo <= 9)
            throw new IllegalArgumentException();
        this.chunkNo = chunkString.toCharArray();
        this.replicationDeg = replicationDeg;
        this.version = version;
    }

    public static Header parseHeader(String header) throws IllegalStateException {
        final String[] args = header.split("\\s+");

        MessageType messageType;

        switch (args[1]) {
            case PutChunk.type:
                messageType = new PutChunk();
                break;
            case Stored.type:
                messageType = new Stored();
                break;
            case GetChunk.type:
                messageType = new GetChunk();
                break;
            case Chunk.type:
                messageType = new Chunk();
                break;
            case Delete.type:
                messageType = new Delete();
                break;
            case Removed.type:
                messageType = new Removed();
                break;
            default:
                throw new IllegalStateException("Unexpected message type: " + args[1]);
        }

        return new Header(
                args[0].toCharArray(),
                messageType,
                UUID.fromString(args[2]),
                UUID.fromString(args[3]),
                Integer.parseInt(args[4]),
                Integer.parseInt(args[5])
        );
    }

    @Override
    public String toString() {
        return Arrays.toString(version) + " " + messageType.getType() +
                " " + senderId + " " + fileId + " " +
                Arrays.toString(chunkNo) + " " + replicationDeg + "\n\r";
    }
}
