package com.chargev.emsp.service.cryptography;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

@Service
public class AESService {
    // 시큐어 코드 표준에 맞춰서 AES 암호화를 수행한다. (GCM 모드 사용)
    // 스트링에 대해서만 암복호화 수행 처리함 
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // 96 bits
    private static final int TAG_SIZE = 128; // 128 bits
    private SecretKeySpec secretKey;

    public void setKey(String hexKey) {
        secretKey = new SecretKeySpec(hexStringToByteArray(hexKey), ALGORITHM);
    }

    private String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                                 + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public String encrypt(String plainText)  {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        if(cipher == null) {
            return null;
        }
        
        byte[] iv = new byte[IV_SIZE];
        SecureRandom random = new SecureRandom();
        random.nextBytes(iv);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
        try {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        byte[] cipherText = null;
        
        try {
            cipherText = cipher.doFinal(plainText.getBytes());
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }

        byte[] encryptedData = new byte[IV_SIZE + cipherText.length];
        System.arraycopy(iv, 0, encryptedData, 0, IV_SIZE);
        System.arraycopy(cipherText, 0, encryptedData, IV_SIZE, cipherText.length);
        
        return byteArrayToHexString(encryptedData);
    }

    public String decrypt(String encryptedData)  {
        Cipher cipher = null; 
        try {
            cipher = Cipher.getInstance(TRANSFORMATION);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        byte[] bEncryptedData = hexStringToByteArray(encryptedData);
        
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(bEncryptedData, 0, iv, 0, IV_SIZE);
        
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);
        
        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        byte[] cipherText = new byte[bEncryptedData.length - IV_SIZE];
        System.arraycopy(bEncryptedData, IV_SIZE, cipherText, 0, cipherText.length);
        
        byte[] plainText = null;
        try {
          plainText = cipher.doFinal(cipherText);  
        } 
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        return new String(plainText);
    }

}
