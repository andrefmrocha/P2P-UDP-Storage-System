package com.feup.sdis.peer;


import com.feup.sdis.actions.BSDispatcher;
import com.feup.sdis.actions.Dispatcher;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import static com.feup.sdis.peer.Constants.SENDER_ID;

public class Peer {
    public static void main(String[] args) {

        BSDispatcher dispatcher = new BSDispatcher(new HashMap<>());
        try {
            final Dispatcher stub = (Dispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
            final Registry registry = LocateRegistry.createRegistry(1099);

            registry.rebind(SENDER_ID, stub);
            System.out.println("Peer " + SENDER_ID + " ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
