package com.feup.sdis.actions;

import com.feup.sdis.model.MessageError;

public class BSDispatcher implements Dispatcher {

    public String processMsg(String msg) throws MessageError {
        final String[] args = msg.split("\\s+");
        Action action;
        System.out.println(msg);
        switch (args[0]){
            case "BACKUP":
                action = new Backup(args);
                break;
            default:
                throw new MessageError("Wrong RMI message received!");
        }
        action.process();
        return "ola, tudo bem?";
    }
}
