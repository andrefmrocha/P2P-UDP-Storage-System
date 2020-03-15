package com.feup.sdis.actions;

import com.feup.sdis.peer.Constants;

public interface Action {
    static String generateId(String fileContent) {  // TODO: Check actual algorithm for file storage
        return  Integer.toString((fileContent + Constants.SENDER_ID).hashCode());
    }

    void process();
}
