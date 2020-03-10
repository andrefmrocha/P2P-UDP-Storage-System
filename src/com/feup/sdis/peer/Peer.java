package com.feup.sdis.peer;


import com.feup.sdis.actions.BSDispatcher;
import com.feup.sdis.actions.Dispatcher;

import java.lang.management.ManagementFactory;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class Peer {
    public static void main(String[] args) {

        BSDispatcher dispatcher = new BSDispatcher(new HashMap<>());
        try {
            final Dispatcher stub = (Dispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
            final Registry registry = LocateRegistry.createRegistry(1099);

            String registryName = "peer-" + ManagementFactory.getRuntimeMXBean().getName();
            registry.rebind(registryName, stub);
            System.out.println("Peer " + registryName + " ready");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
