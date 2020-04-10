package com.feup.sdis.actions;

import com.feup.sdis.peer.Constants;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public interface Action {

    static String generateId(File file) {
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            long lastMod = file.lastModified();
            return generateId(fileContent, lastMod);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "error";
    }

    static String generateId(byte[] fileContent, long lastModified) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write( fileContent );
            outputStream.write( ByteBuffer.allocate(Long.BYTES).putLong(lastModified).array() );
            byte[] hash = md.digest(outputStream.toByteArray());

            String hex = String.format("%064x", new BigInteger(1, hash));
            return hex;
        }
        catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return Integer.toString((fileContent + Constants.SENDER_ID).hashCode());
    }

    String process();
}
