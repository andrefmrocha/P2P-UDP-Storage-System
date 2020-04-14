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

        if (args.length != 9) {
            System.out.println("Invalid number of arguments");
            return;
        }

        String protocolVersion = args[0];
        String peerID = args[1];
        String accessPoint = args[2];
        String MCAddress = args[3];
        String MDBAddress = args[5];
        String MDRAddress = args[7];
        int MCPort;
        int MDBPort;
        int MDRPort ;

        if(protocolVersion.equals("1.1")){
            enhanced = true;
        }

        try {
            Integer.parseInt(peerID);
            MCPort = Integer.parseInt(args[4]);
            MDBPort = Integer.parseInt(args[6]);
            MDRPort = Integer.parseInt(args[8]);
        } catch (NumberFormatException nfe) {
            System.out.println("Ports must be numeric");
            return;
        }
        Constants.SENDER_ID = peerID;
        Constants.peerRootFolder = Constants.peerParentFolder + "peer-" + peerID + "/";
        Constants.backupFolder = Constants.peerRootFolder + "backups/";
        Constants.restoredFolder = Constants.peerRootFolder + "restored/";
        Constants.MC_CHANNEL = MCAddress;
        Constants.MC_PORT = MCPort;
        Constants.MDB_CHANNEL = MDBAddress;
        Constants.MDB_PORT = MDBPort;
        Constants.MDR_CHANNEL = MDRAddress;
        Constants.MDR_PORT = MDRPort;

        BSDispatcher dispatcher = new BSDispatcher();
        try {
            final Dispatcher stub = (Dispatcher) UnicastRemoteObject.exportObject(dispatcher, 0);
            try {
                LocateRegistry.createRegistry(Constants.RMI_PORT);
            }
            catch (ExportException e) {} // already exists
            final Registry registry = LocateRegistry.getRegistry(Constants.RMI_PORT);
            registry.rebind(accessPoint, stub);

            System.out.println("*\nStarting Peer " + SENDER_ID);
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
