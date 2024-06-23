package com.chargev.emsp.entity.keyentity;

import lombok.Data;

@Data
public class KeyRequest {
    private String clientId; // 요청하는 클라이언트 ID
    private String clientSecret; // 요청하는 클라이언트 SECRET
    private String pairSecret; // 요청하는 키가 암호화를 요구하는 키 (비밀키)일 경우, 해당 키 자체의 암호화를 처리하기 위한 키값
    private int keyType; // 키타입  1: 공개키, 2: 비밀키, 3 : 공개키 + 비밀키 4: 대칭키
    private String publicKey; // 공개 키일 경우, 이 키값만 제공, 
    private String privateKey; // 비밀 키일 경우, 
}
