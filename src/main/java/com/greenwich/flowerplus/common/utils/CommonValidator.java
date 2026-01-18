package com.greenwich.flowerplus.common.utils;

public class CommonValidator {

    private CommonValidator() {
        // Private constructor to prevent instantiation
    }

    public static String normalizeString(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.trim().toLowerCase();
    }


    
}
