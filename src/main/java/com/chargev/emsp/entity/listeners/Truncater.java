package com.chargev.emsp.entity.listeners;
import java.nio.charset.StandardCharsets;

public class Truncater {
    public static String truncateIfOver(String input, int maxBytes){

        if (input != null && input.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            return truncateToMaxBytes(input, maxBytes);
        }

        return input;
    }
    private static String truncateToMaxBytes(String input, int maxBytes) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxBytes) {
            return input;
        }

        int length = maxBytes;
        while (length > 0 && (bytes[length] & 0xC0) == 0x80) {
            length--;
        }

        return new String(bytes, 0, length, StandardCharsets.UTF_8);
    }
}
