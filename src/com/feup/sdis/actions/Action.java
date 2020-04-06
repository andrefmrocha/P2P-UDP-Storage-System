package com.feup.sdis.actions;

import com.feup.sdis.peer.Constants;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public interface Action {
    static String generateId(String fileContent) {  // TODO: Check actual algorithm for file storage
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(fileContent.getBytes(StandardCharsets.UTF_8));
            String hex = String.format("%064x", new BigInteger(1, hash));
            return hex;
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return  Integer.toString((fileContent + Constants.SENDER_ID).hashCode());
    }

    String process();
}
