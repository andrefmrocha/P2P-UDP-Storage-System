package com.feup.sdis.actions;

import java.util.HashMap;

public class BSDispatcher implements Dispatcher {

    private HashMap<String, String> table;

    public BSDispatcher(HashMap<String, String> table) {
        this.table = table;
    }

    public String processMsg(String msg) {
        System.out.println("Processing msg:  " + msg);
        return "ola, tudo bem?";
    }
}
