package com.feup.sdis.model;

public class MessageError extends Exception {
    final String errorMsg;

    public MessageError(String s) {
        errorMsg = s;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
