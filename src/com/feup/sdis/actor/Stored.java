package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SerializableHashMap;
import com.feup.sdis.model.Store;
import com.feup.sdis.peer.Constants;

import java.util.HashSet;
import java.util.Set;

import java.util.HashSet;
import java.util.Set;

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
        final Set<String> currentReplications =
                replCounter.getOrDefault(chunkId, new HashSet<>());
        if (!currentReplications.contains(message.getHeader().getSenderId())) {
            if (message.getHeader().getVersion().equals(Constants.enhancedVersion) &&
                    Store.instance().getBackedUpFiles().get(message.getHeader().getFileId()) != null &&
                    currentReplications.size() >= message.getHeader().getReplicationDeg())
                this.removeExcess(senderPeerId);
            else {
                System.out.println("Updated replication table for chunk " + chunkId + ", added peer " + senderPeerId);
                currentReplications.add(senderPeerId);
                replCounter.put(chunkId, currentReplications);
            }

        }
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
