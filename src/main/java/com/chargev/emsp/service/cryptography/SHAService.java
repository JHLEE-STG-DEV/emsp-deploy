package com.chargev.emsp.service.cryptography;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.pnc.HashAlgorithm;

@Service
public class SHAService {
    public String hash(HashAlgorithm algorithm, String input){
        if(algorithm.equals(HashAlgorithm.SHA256)){
            return sha256Hash(input, "");
        } else if(algorithm.equals(HashAlgorithm.SHA384)){
            return sha384Hash(input, "");
        } else if(algorithm.equals(HashAlgorithm.SHA512)){
            return sha512Hash(input, "");
        }else{
            return null;
        }
    }
    public String sha256Hash(String input, String salt) {
        // input = salt + input;

        // try {
        //     MessageDigest md = MessageDigest.getInstance("SHA-256");
        //     byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        //     BigInteger number = new BigInteger(1, hash);
        //     StringBuilder hexString = new StringBuilder(number.toString(16));
        //     while (hexString.length() < 32) {
        //         hexString.insert(0, '0');
        //     }
        //     return hexString.toString();
        // } catch (Exception e) {
        //     return null;
        // }
        return generateHash(input, salt, "SHA-256", 64);
    }

    public String sha384Hash(String input, String salt) {
        return generateHash(input, salt, "SHA-384", 96);
    }

    public String sha512Hash(String input, String salt) {
        return generateHash(input, salt, "SHA-512", 128);
    }
    private String generateHash(String input, String salt, String algorithm, int length) {
        input = salt + input;
        
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < length) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}