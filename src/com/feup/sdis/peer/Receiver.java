package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.MessageError;
import com.feup.sdis.model.SocketFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public abstract class Receiver implements Runnable {
    abstract MessageActor parseMessage(String msg) throws MessageError;
    abstract int getPort();
    abstract String getChannel();

    @Override
    public void run() {
        try { // TODO: Since this creates a socket on the moment it uses it, peer receive their own messages. This must be fixed, however this is good for debugging for now.
            final MulticastSocket socket = SocketFactory.buildMulticastSocket(getPort(), getChannel());
            while (true) {
                byte[] buf = new byte[Constants.packetSize];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received new message: " + msg);
                new Thread(()-> {
                    try {
                        MessageActor actor = this.parseMessage(msg);
                        actor.process();
                    } catch (IOException | MessageError e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
