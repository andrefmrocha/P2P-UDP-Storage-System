package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message {
    private final Header header;
    private final byte[] body;

    public Message(Header header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public Message(Header header) {
        this(header, null);
    }

    public byte[] getBody() {
        return body;
    }

    public Header getHeader() {
        return header;
    }

    public DatagramPacket generatePacket(InetAddress group, int port) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        try {
            outputStream.write((header.toString() + "\r\n").getBytes());
            if(body != null)
                outputStream.write(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        final byte[] message = outputStream.toByteArray();
        return new DatagramPacket(message, message.length, group, port);
    }
}
