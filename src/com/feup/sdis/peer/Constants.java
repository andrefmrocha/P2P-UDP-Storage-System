package com.feup.sdis.peer;

import java.lang.management.ManagementFactory;

public class Constants {
    private Constants() {} // no way to instantiate this class
    public static final String MC_CHANNEL = "224.0.0.0";
    public static final String MDB_CHANNEL = "224.0.0.1";
    public static final String MDR_CHANNEL = "224.0.0.2";
    public static final int MC_PORT = 8080;
    public static final int MC_TTL = 1;
    public static final int MC_TIMEOUT = 3000;
    public static final String SENDER_ID = "peer-" + ManagementFactory.getRuntimeMXBean().getName().trim();
    public static final int packetSize = (int) (Math.pow(2, 8) + Math.pow(2, 3));
    public static final String version = "1.0";
    public static final int MAX_FILE_SIZE = 6400000;
    public static final int BLOCK_SIZE = 64000;
    public static final int MAX_PUT_CHUNK_TRIES = 5;
    public static final int MAX_GET_CHUNK_TRIES = 5;
    public static String backupFolder = "backups/";
    public static String restoredFolder = "restored/";
}