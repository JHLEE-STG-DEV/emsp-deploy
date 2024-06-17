package com.chargev.emsp.service.cryptography;

import java.security.MessageDigest;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SHAUtil {
    public String sha256Hash(String input) {
        input = "a557625411294ad283897ddb8900fefa" + input;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            BigInteger number = new BigInteger(1, hash);

            StringBuilder hexString = new StringBuilder(number.toString(16));

            while (hexString.length() < 32) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (Exception e) {
        }
        return null;
    }
}