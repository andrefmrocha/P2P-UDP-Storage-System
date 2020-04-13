package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.Store;

public class Delete extends MessageActor {
    final static public String type =  "DELETE";

    public Delete(Message message) {
        super(message);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void process() {
        Header header = message.getHeader();
        String fileID = header.getFileId();
        com.feup.sdis.actions.Delete.deleteFileChunks(fileID,
                header.getReplicationDeg(),
                header.getVersion(),
                scheduler);
        if(Store.instance().getBackedUpFiles().containsKey(fileID))
            Store.instance().getBackedUpFiles().remove(fileID);
    }

    @Override
    public boolean hasBody() {
        return false;
    }
}
