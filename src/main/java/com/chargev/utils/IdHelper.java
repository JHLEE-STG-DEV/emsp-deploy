package com.chargev.utils;

import java.util.UUID;

public class IdHelper {
    public static String genLowerUUID32(){
            return UUID.randomUUID().toString().toLowerCase().replace("-","");
    }
}
