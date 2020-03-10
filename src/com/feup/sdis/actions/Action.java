package com.feup.sdis.actions;

import java.util.HashMap;

public interface Action {
    String process(HashMap<String, String> table);
}
