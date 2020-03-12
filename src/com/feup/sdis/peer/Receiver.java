package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Hashtable;
import java.util.Map;

public class Receiver implements Runnable {
    @Override
    public void run() {
        try { // TODO: Since this creates a socket on the moment it uses it, peer receive their own messages. This must be fixed, however this is good for debugging for now.
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT);
            socket.joinGroup(InetAddress.getByName(Constants.MC_CHANNEL));
            socket.setTimeToLive(Constants.MC_TTL);
            if(!(new File(Constants.SENDER_ID)).mkdir()){
                System.out.println("Failed to create directory!");
            }
            final Map<String, Integer> files = new Hashtable<>();

            while (true) {
                byte[] buf = new byte[Constants.packetSize];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received new message: " + msg);
                new Thread(()-> {
                    try {
                        MessageActor actor = Message.parseMessage(msg);
                        actor.process(files);
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
