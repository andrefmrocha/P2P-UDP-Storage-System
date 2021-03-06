package com.feup.sdis.actions;

import com.feup.sdis.model.MessageError;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BSDispatcher implements Dispatcher {

    public String processMsg(String msg) throws MessageError {
        final String[] args = msg.split(",");
        Action action;
        System.out.println("\n" + new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date())
                + " Received client request: "
                + msg);
        switch (args[0]){
            case "BACKUP":
                action = new Backup(args);
                break;
            case "DELETE":
                action = new Delete(args);
                break;
            case "RESTORE":
                action = new Restore(args);
                break;
            case "STATE":
                action = new State();
                break;
            case "RECLAIM":
                action = new Reclaim(args);
                break;
            default:
                throw new MessageError("Received unknown RMI message");
        }
        return action.process();
    }
}
