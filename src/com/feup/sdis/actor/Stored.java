package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

public class Stored extends MessageActor {
    final static public String type = "STORED";

    public Stored(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        final String chunkId = message.getHeader().getChunkId();
        final String senderPeerId = message.getHeader().getSenderId();
        final SerializableHashMap replCounter = Store.instance().getReplCount();
        if (!replCounter.containsPeer(chunkId, senderPeerId)) {
            if (message.getHeader().getVersion().equals(Constants.enhancedVersion) &&
                    Store.instance().getBackedUpFiles().get(message.getHeader().getFileId()) != null &&
                    replCounter.getSize(chunkId) >= message.getHeader().getReplicationDeg()) {
                System.out.println("Sending EXCESS msg to peer " + senderPeerId);
                this.removeExcess(senderPeerId);
            }
            else {
                System.out.println("Updated replication table for chunk " + chunkId + ", added peer " + senderPeerId);
                replCounter.addNewID(chunkId, senderPeerId);
            }

        }
        else
            System.out.println("Peer is already in replication table");
    }

    private void removeExcess(String senderPeerId) {
        final Header header = new Header(Constants.enhancedVersion, Excess.type,
                Constants.SENDER_ID, message.getHeader().getFileId(), Integer.parseInt(message.getHeader().getChunkNo()),
                -1, senderPeerId);
        this.sendMessage(Constants.MC_PORT, Constants.MC_CHANNEL, new Message(header));
    }


    @Override
    public boolean hasBody() {
        return false;
    }
}
