package com.feup.sdis.model;

import java.util.Arrays;
import java.util.UUID;

enum MESSAGE_TYPE {
    PUTCHUNK,
    STORED,
    GETCHUNK,
    CHUNK,
    DELETE,
    REMOVED
}

public class Header {
    private char[] version;
    private MESSAGE_TYPE messageType;
    private UUID senderId;
    private UUID fileId;
    private char[] chunkNo;
    private int replicationDeg;

    public Header(char[] version, MESSAGE_TYPE messageType, UUID senderId, UUID fileId, int chunkNo, int replicationDeg) {
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        final String chunkString = Integer.toString(chunkNo);
        if(version.length != 3  || chunkString.length() <= 6  || chunkNo <= 9)
            throw new IllegalArgumentException();
        this.chunkNo = chunkString.toCharArray();
        this.replicationDeg = replicationDeg;
        this.version = version;
    }

    public static Header parseHeader(String header) throws IllegalStateException{
        final String[] args = header.split("\\s+");

        MESSAGE_TYPE messageType;

        switch (args[1]){
            case "PUTCHUNK":
                messageType = MESSAGE_TYPE.PUTCHUNK;
                break;
            case "STORED":
                messageType = MESSAGE_TYPE.STORED;
                break;
            case "GETCHUNK":
                messageType = MESSAGE_TYPE.GETCHUNK;
                break;
            case "CHUNK":
                messageType = MESSAGE_TYPE.CHUNK;
                break;
            case "DELETE":
                messageType = MESSAGE_TYPE.DELETE;
                break;
            case "REMOVED":
                messageType = MESSAGE_TYPE.REMOVED;
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
        return Arrays.toString(version) + " " + messageType.name() +
                " " + senderId + " " + fileId + " " +
                Arrays.toString(chunkNo) + " " + replicationDeg + "\n\r";
    }
}
