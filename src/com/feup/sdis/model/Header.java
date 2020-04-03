package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

public class Header {
    private String version;
    private String messageType;
    private String senderId;
    private String fileId;
    private String chunkNo;
    private int replicationDeg;

    public String getVersion() {
        return version;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getFileId() {
        return fileId;
    }

    public String getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }

    public Header(String version, String messageType, String senderId, String fileId, int chunkNo, int replicationDeg) {
        this.messageType = messageType;
        this.senderId = senderId;
        this.fileId = fileId;
        final String chunkString = Integer.toString(chunkNo);
        if (version.length() != 3 || chunkString.length() > 6 || replicationDeg > 9)
            throw new IllegalArgumentException();
        this.chunkNo = chunkString;
        this.replicationDeg = replicationDeg;
        this.version = version;
    }

    public Header(String version, String messageType, String senderId, String fileId, int chunkNo) {
        this(version, messageType, senderId, fileId, chunkNo, -1);
    }

    public Header(String version, String messageType, String senderId, String fileId) {
        this(version, messageType, senderId, fileId, -1, -1);
    }

    public static Header parseHeader(String header) throws MessageError {
        final String[] args = header.split("\\s+");
        int replicationDeg = -1;
        int chunkNo = -1;
        if (args.length < 4)
            throw new MessageError("Missing Header parameters!");
        if (args.length >= 5)
            chunkNo = Integer.parseInt(args[4]);
        if (args.length == 6)
            replicationDeg = Integer.parseInt(args[5]);


        return new Header(
                args[0],
                args[1],
                args[2],
                args[3],
                chunkNo,
                replicationDeg
        );
    }

    public String getChunkId(){
        return getFileId() + Constants.idSeparation + getChunkNo();
    }

    @Override
    public String toString() {
        return version + " " + messageType +
                " " + senderId + " " + fileId + " " +
                (chunkNo.equals("-1") ? "" : chunkNo) + " " + (replicationDeg == -1 ? "" : replicationDeg) + "\n\r";
    }
}
