package com.feup.sdis.peer;


import com.feup.sdis.actions.BSDispatcher;
import com.feup.sdis.actions.Dispatcher;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static com.feup.sdis.peer.Constants.SENDER_ID;

public class Peer {
    public static boolean enhanced = false;
    public static void main(String[] args) {
        if(args.length == 1 && args[0].equals("ENHANCED")){
            enhanced = true;
        }

        BSDispatcher dispatcher = new BSDispatcher();
        try {
            final Dispatcher stub = (Dispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
            final Registry registry = LocateRegistry.createRegistry(1099);

            registry.rebind("MEIAS", stub);
            if(!(new File(Constants.SENDER_ID)).mkdir()){
                System.out.println("Failed to create peer directory!");
            }
            if(!(new File(Constants.SENDER_ID + "/" + Constants.backupFolder)).mkdir()){
                System.out.println("Failed to create backups directory!");
            }
            if(!(new File(Constants.SENDER_ID + "/" + Constants.restoredFolder)).mkdir()){
                System.out.println("Failed to create restored directory!");
            }
            System.out.println("Peer " + SENDER_ID + " ready");
            new Thread(new ControlChannelReceiver()).start();
            new Thread(new DataBackupChannelReceiver()).start();
            new Thread(new DataRestoreChannelReceiver()).start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
