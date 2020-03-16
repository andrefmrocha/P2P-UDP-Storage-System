package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Random;

public abstract class MessageActor {
    protected final Message message;
    private final Random random = new Random();

    protected MessageActor(Message message) {
        this.message = message;
    }

    public static String parseBody(String msg){
        return msg.substring(msg.indexOf("\n\r\n\r") + 4);
    }

    protected void sendMessage(int port, String groupChannel, Message msg) throws IOException {
        try {
            Thread.sleep(random.nextInt(400 + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        final MulticastSocket socket = new MulticastSocket(port);
        final InetAddress group = InetAddress.getByName(groupChannel);
        socket.joinGroup(group);
        socket.setTimeToLive(1);
        socket.setSoTimeout(Constants.MC_TIMEOUT);
        final DatagramPacket datagramPacket = message.generatePacket(group, Constants.MC_PORT);
        socket.send(datagramPacket);
    }

    abstract String getType();
    public abstract void process() throws IOException;
    abstract boolean hasBody();
}
