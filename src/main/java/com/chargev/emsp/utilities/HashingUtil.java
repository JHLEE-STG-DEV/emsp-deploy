package com.chargev.emsp.utilities;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class HashingUtil {

    public static String sha256Hash(String filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            InputStream fis = new FileInputStream(filePath);
            byte[] byteArray = new byte[1024];
            int bytesCount = 0;

            // Read file data and update in message digest
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }

            fis.close();

            // Get the hash's bytes
            byte[] bytes = digest.digest();

            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Could not generate hash from file", e);
        }
    }
}