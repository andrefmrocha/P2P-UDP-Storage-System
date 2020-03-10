package com.feup.sdis.client;

import com.feup.sdis.actions.Dispatcher;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) throws IOException, NotBoundException {
        if(args.length != 4) {
            System.out.println("Usage: java AppName <peer_ap> <operation> <opnd_1> <opnd_2>");
            return;
        }

        final String peer_ap = args[0];
        final String operation = args[1];
        final String opnd_1 = args[2];
        final int opnd_2 = Integer.parseInt(args[3]);

        // check valid operation

        Registry registry = LocateRegistry.getRegistry("localhost");
        Dispatcher stub = (Dispatcher) registry.lookup(peer_ap);
        String answer = stub.processMsg("oi");

        System.out.println("Answer: " + answer);

    }
}