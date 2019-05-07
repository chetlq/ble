package com.example.mybattarymonitor;

import java.util.HashMap;
import java.util.UUID;

/**
 * Created by brijesh on 15/4/17.
 */

public class SampleGattAttributes {
    public static String TIME_SERVICE = "00001805-0000-1000-8000-00805f9b34fb";
//    /* Mandatory Current Time Information Characteristic */
//    public static String CURRENT_TIME    = "00002a2b-0000-1000-8000-00805f9b34fb";
//    /* Optional Local Time Information Characteristic */
    public static String LOCAL_TIME_INFO = ("00002a0f-0000-1000-8000-00805f9b34fb");
//

//    public static String TIME_SERVICE = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    /* Mandatory Current Time Information Characteristic */
    public static String CURRENT_TIME    = "00002a2b-0000-1000-8000-00805f9b34fb";
    /* Optional Local Time Information Characteristic */
//    public static String LOCAL_TIME_INFO = ("6e400002-b5a3-f393-e0a9-e50e24dcca9e");


    private static HashMap<String, String> attributes = new HashMap();

    static {

        attributes.put(TIME_SERVICE, "Battery Service1");
        attributes.put(CURRENT_TIME, "Battery Service2");
        attributes.put(LOCAL_TIME_INFO, "Battery Service3");
    }

    public static String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name;
    }
}