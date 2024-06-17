package com.chargev.emsp.utilities;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class AESCipher {

    private static final String AES = "AES";
    private static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    private SecretKeySpec secretKey;
    private IvParameterSpec iv;

    public void init(String key, String iv) {
        this.secretKey = new SecretKeySpec(hexStringToByteArray(key), AES);
        this.iv = new IvParameterSpec(hexStringToByteArray(iv));
    }

    public void init(int kind) {
        this.secretKey = new SecretKeySpec(hexStringToByteArray("39282fd1d10c4a97ae71a43d6b6f5cb6"), AES);
        this.iv = new IvParameterSpec(hexStringToByteArray("82ba7c0b842d4eeeba1c36303ff624ed"));
    }

    public void init() {
        this.secretKey = new SecretKeySpec(hexStringToByteArray("8e01fa2c147442578ae53f4c8dc42bb34cda7701519745f08db3b986532c6367"), AES);
        this.iv = new IvParameterSpec(hexStringToByteArray("342323ec2d344f5e99a0fb5bb4f6c06d"));
    }

    public AESCipher(byte[] key, byte[] iv) {
        this.secretKey = new SecretKeySpec(key, AES);
        this.iv = new IvParameterSpec(iv);
    }

    public String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return byteArrayToHex(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.toString());
        }
    }

    public String decrypt(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] original = cipher.doFinal(hexStringToByteArray(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.toString());
        }
    }

    public String encryptBase64(String value) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public String decryptBase64(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
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

    private String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for(byte b: a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}