package com.feup.sdis.model;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Message {
    private final Header header;
    private final String body;

    public Message(Header header, String body) {
        this.header = header;
        this.body = body;
    }

    public Message(Header header) {
        this(header, null);
    }

    public String getBody() {
        return body;
    }

    public Header getHeader() {
        return header;
    }

    public DatagramPacket generatePacket(InetAddress group, int port) {
        final byte[] message = (header.toString() + "\n\r" +(body == null ? "" : body)).getBytes();
        return new DatagramPacket(message, message.length, group, port);
    }
}
