package com.chargev.emsp.service.cryptography;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.keyentity.Keys;
import com.chargev.emsp.repository.key.KeyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KeyService {
    private final KeyRepository keyRepository;
    
    public Keys getKeys(String clientId, String clientSecret, String keyType) {
        return keyRepository.findByClientIdAndClientSecret(clientId, clientSecret, keyType);
    }

    public Keys saveKeys(Keys keys) {
        return keyRepository.save(keys);
    }

    public boolean deleteKeys(String keyId) {
        Keys keys = keyRepository.findById(keyId).orElse(null);
        if(keys == null) {
            return false;
        }
        if(keys.getDeleted() == 1) {
            return true; // 이미 삭제된 상태
        }
        keys.setDeleted(1);
        keyRepository.save(keys);

        keys = keyRepository.findById(keyId).orElse(null);
        if(keys == null) {
            return false;
        }
        return keys.getDeleted() == 1;
    }
}
