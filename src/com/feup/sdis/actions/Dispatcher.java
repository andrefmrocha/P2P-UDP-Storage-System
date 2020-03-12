package com.feup.sdis.actions;

import com.feup.sdis.model.MessageError;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Dispatcher extends Remote {
    String processMsg(String msg) throws RemoteException, MessageError;
}
