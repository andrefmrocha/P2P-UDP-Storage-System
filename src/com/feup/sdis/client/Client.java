package com.feup.sdis.client;

import com.feup.sdis.actions.Dispatcher;
import com.feup.sdis.model.MessageError;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Client {

    public static void main(String[] args) throws IOException, NotBoundException {
        if(args.length < 2) {
            System.out.println("Usage: java AppName <peerAp> <operation> <opnd1> [<opnd2>]");
            return;
        }

        String msg = null;
        if (args.length == 2){
            msg = args[1];
        }
        else if(args.length == 3){
            msg = args[1] + "," + args[2];
        } else if(args.length == 4){
            msg = args[1] + "," + args[2] + "," + args[3];
        }

        final String peerAp = args[0];

        System.out.println(new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date()));
        Registry registry = LocateRegistry.getRegistry("localhost");
        Dispatcher stub = (Dispatcher) registry.lookup(peerAp);
        String answer = null;
        try {
            System.out.println("@ Sending message to peer " + peerAp + ": " + msg);
            answer = stub.processMsg(msg);
        } catch (MessageError messageError) {
            System.out.println(messageError.getErrorMsg());
        }
        System.out.println("@ Received answer:");
        System.out.println(answer + "\n");

    }
}