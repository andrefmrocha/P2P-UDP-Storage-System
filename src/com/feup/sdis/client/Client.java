package com.feup.sdis.client;

import com.feup.sdis.actions.Dispatcher;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {

    public static void main(String[] args) throws IOException, NotBoundException {
        if(args.length != 4) {
            System.out.println("Usage: java AppName <peerAp> <operation> <opnd1> <opnd2>");
            return;
        }

        final String peerAp = args[0];
        final String operation = args[1];
        final String opnd1 = args[2];
        final int opnd2 = Integer.parseInt(args[3]);

        // check valid operation

        Registry registry = LocateRegistry.getRegistry("localhost");
        Dispatcher stub = (Dispatcher) registry.lookup(peerAp);
        String answer = stub.processMsg("oi");

        System.out.println("Answer: " + answer);

    }
}