package com.feup.sdis.model;

import java.util.Arrays;
import java.util.UUID;

public class Header {
    private char[] version;
    private String messageType;
    private UUID senderId;
    private UUID fileId;
    private char[] chunkNo;
    private int replicationDeg;

    public char[] getVersion() {
        return version;
    }

    public String getMessageType() {
        return messageType;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public UUID getFileId() {
        return fileId;
    }

    public char[] getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }

    public Header(char[] version, String messageType, UUID senderId, UUID fileId, int chunkNo, int replicationDeg) {
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

    public static Header parseHeader(String header) throws MessageError {
        final String[] args = header.split("\\s+");
        int replicationDeg = -1;
        if (args.length < 5) {
            throw new MessageError("Missing Header parameters!");
        } else if (args.length == 6)
            replicationDeg = Integer.parseInt(args[5]);


        return new Header(
                args[0].toCharArray(),
                args[1],
                UUID.fromString(args[2]),
                UUID.fromString(args[3]),
                Integer.parseInt(args[4]),
                replicationDeg
        );
    }

    @Override
    public String toString() {
        return Arrays.toString(version) + " " + messageType +
                " " + senderId + " " + fileId + " " +
                Arrays.toString(chunkNo) + " " + (replicationDeg == -1 ? "" : replicationDeg) + "\n\r";
    }
}
