package com.feup.sdis.peer;

import com.feup.sdis.actor.MessageActor;
import com.feup.sdis.model.Message;
import com.feup.sdis.model.MessageError;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Receiver implements Runnable {
    @Override
    public void run() {
        try {
            final MulticastSocket socket = new MulticastSocket(Constants.MC_PORT);
            socket.joinGroup(InetAddress.getByName(Constants.MC_CHANNEL));
            socket.setTimeToLive(Constants.MC_TTL);
            socket.setSoTimeout(Constants.MC_TIMEOUT);
            if(!(new File(Constants.registryName)).mkdir()){
                System.out.println("Failed to create directory!");
            }
            final Set<UUID> fileSet = new HashSet<>();

            while (true) {
                byte[] buf = new byte[Constants.packetSize];
                final DatagramPacket packet = new DatagramPacket(buf, buf.length);

                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());
                new Thread(()-> {
                    try {
                        MessageActor actor = Message.parseMessage(msg);
                        actor.process(fileSet);
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
