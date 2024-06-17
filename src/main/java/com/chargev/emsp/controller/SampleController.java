package com.chargev.emsp.controller;

import java.util.Base64;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.entity.Credentials;
import com.chargev.emsp.service.CredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("sample")
@Validated
@RequiredArgsConstructor
public class SampleController {
        private final CredentialService credentialService;

        @GetMapping()
        public ResponseEntity<?> getSample(){
            String headerStr = "sampleAuthHeader";
        byte[] tokenByte = Base64.getDecoder().decode(headerStr);
        String token = new String(tokenByte);

        Optional<Credentials> getResult = credentialService.getCredentailsWithTokenJPA(token);

        if(getResult.isPresent()){
            return ResponseEntity.ok().body(getResult.get());

        }else{
            return ResponseEntity.noContent().build();
        }


        }

}
