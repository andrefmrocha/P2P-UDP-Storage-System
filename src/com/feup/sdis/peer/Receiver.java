package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.SocketFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Receiver implements Runnable {
    abstract MessageActor parseMessage(String msg) throws MessageError;
    abstract int getPort();
    abstract String getChannel();

    @Override
    public void run() {
        try {
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(getPort(), getChannel());
            final ExecutorService pool = Executors.newCachedThreadPool();
            while (true) {
                byte[] buf = new byte[Constants.packetSize];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());

                String[] parts = msg.split(" ");
                if (parts.length < 3) {
                    System.out.println("Received malformed message: " + msg);
                    continue;
                }
                if (parts[2].equals(Constants.SENDER_ID)) continue; // drop packets from same peer


                System.out.println("\nReceived new message: " + msg.split("\\r?\\n")[0]);
                pool.execute(()-> {
                    try {
                        MessageActor actor = this.parseMessage(msg);
                        actor.process();
                    } catch (IOException | MessageError e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
