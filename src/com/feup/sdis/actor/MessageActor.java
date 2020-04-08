package com.feup.sdis.actor;

import com.feup.sdis.model.Header;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.SocketFactory;
import com.feup.sdis.peer.Constants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class MessageActor {
    protected final Message message;
    protected final Random random = new Random();
    protected final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(0);


    protected MessageActor(Message message) {
        this.message = message;
    }

    public static byte[] parseBody(String msg, byte[] msgBytes) {
        final int sliceI = msg.indexOf("\n\r\n\r") + 4;
        return Arrays.copyOfRange(msgBytes, sliceI, msgBytes.length);
    }

    protected void sendMessage(int port, String groupChannel, Message msg) {
        scheduler.schedule(() -> {
            try {
                final InetAddress group = InetAddress.getByName(groupChannel);
                final MulticastSocket socket = SocketFactory.buildMulticastSocket(port, group);
                final DatagramPacket datagramPacket = msg.generatePacket(group, Constants.MC_PORT);
                socket.send(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, random.nextInt(400 + 1), TimeUnit.MILLISECONDS);
    }

    abstract String getType();

    public abstract void process() throws IOException;

    abstract boolean hasBody();
}
