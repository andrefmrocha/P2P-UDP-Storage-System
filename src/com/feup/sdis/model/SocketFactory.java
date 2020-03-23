package com.feup.sdis.model;

import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketFactory {
    public static MulticastSocket buildMulticastSocket(int port, String channel) throws IOException {
        final MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(InetAddress.getByName(channel));
        socket.setTimeToLive(Constants.MC_TTL);
        return socket;
    }

    public static MulticastSocket buildMulticastSocket(int port, InetAddress group) throws IOException {
        final MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(group);
        socket.setTimeToLive(Constants.MC_TTL);
        return socket;
    }
}
