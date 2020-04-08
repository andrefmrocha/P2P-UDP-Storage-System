package com.feup.sdis.peer;


import com.feup.sdis.actions.BSDispatcher;
import com.feup.sdis.actions.Dispatcher;

import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ExportException;
import java.rmi.server.UnicastRemoteObject;

import static com.feup.sdis.peer.Constants.SENDER_ID;

public class Peer {
    public static boolean enhanced = false;
    public static void main(String[] args) {

        if (args.length == 0 || args.length > 2) {
            System.out.println("Invalid number of arguments");
            return;
        }

        if(args.length == 2 && args[1].equals("ENHANCED")){
            enhanced = true;
        }

        try {
            Integer.parseInt(args[0]);
        } catch (NumberFormatException nfe) {
            System.out.println("ID must be an integer");
            return;
        }
        Constants.SENDER_ID = args[0];
        Constants.peerRootFolder = Constants.peerParentFolder + "peer-" + args[0] + "/";
        Constants.backupFolder = Constants.peerRootFolder + "backups/";
        Constants.restoredFolder = Constants.peerRootFolder + "restored/";

        BSDispatcher dispatcher = new BSDispatcher();
        try {
            final Dispatcher stub = (Dispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
            try {
                LocateRegistry.createRegistry(Constants.RMI_PORT);
            }
            catch (ExportException e) {} // already exists
            final Registry registry = LocateRegistry.getRegistry(Constants.RMI_PORT);
            registry.rebind(Constants.SENDER_ID, stub);

            createPeerFolders();
            System.out.println("Peer " + Constants.SENDER_ID + " ready");

            new Thread(new ControlChannelReceiver()).start();
            new Thread(new DataBackupChannelReceiver()).start();
            new Thread(new DataRestoreChannelReceiver()).start();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void createPeerFolders() {
        if(!(new File(Constants.peerParentFolder)).mkdir()){
            System.out.println("Folder already exists or failed to be created: " + Constants.peerParentFolder);
        }
        if(!(new File(Constants.peerRootFolder)).mkdir()){
            System.out.println("Folder already exists or failed to be created: " + Constants.peerRootFolder);
        }
        if(!(new File(Constants.backupFolder)).mkdir()){
            System.out.println("Folder already exists or failed to be created: " + Constants.backupFolder);
        }
        if(!(new File(Constants.restoredFolder)).mkdir()){
            System.out.println("Folder already exists or failed to be created: " + Constants.restoredFolder);
        }
    }
}
