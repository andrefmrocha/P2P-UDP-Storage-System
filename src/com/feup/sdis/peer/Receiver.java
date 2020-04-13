package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.SocketFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Receiver implements Runnable {
    abstract MessageActor parseMessage(DatagramPacket packet) throws MessageError;
    abstract int getPort();
    abstract String getChannel();
    private static final ExecutorService pool = Executors.newCachedThreadPool();


    @Override
    public void run() {
        try {
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(getPort(), getChannel());
            while (true) {
                byte[] buf = new byte[Constants.BLOCK_SIZE + Constants.PACKET_HEADER_PADDING];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());

                String[] parts = msg.split(" ");
                if (parts.length < 3) {
                    System.out.println("Received malformed message: " + msg);
                    continue;
                }
                if (parts[2].equals(Constants.SENDER_ID)) continue; // drop packets from same peer

                System.out.println("\n" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date())
                        + " Received new message: "
                        + msg.split("\\r?\\n")[0]);
                pool.execute(()-> {
                    try {
                        MessageActor actor = this.parseMessage(packet);
                        actor.process();
                    } catch (IOException | MessageError e) {
                        System.err.println("Error in peer " + Constants.SENDER_ID);
                        e.printStackTrace();
                    }
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
