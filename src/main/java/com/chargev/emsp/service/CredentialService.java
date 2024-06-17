package com.chargev.emsp.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.Credentials;
import com.chargev.emsp.mapper.CredentialsMapper;
import com.chargev.emsp.repository.CredentialsRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CredentialService {
    private final CredentialsMapper mapper;
    private final CredentialsRepository repo;

    
    public Optional<Credentials> getCredentailsWithToken(String token){
        return Optional.ofNullable(mapper.selectCredentialsByToken(token));
    }
    public Optional<Credentials> getCredentailsWithTokenJPA(String token){
        return repo.findByToken(token);
    }

    public void saveCredentialWithMapper(Credentials credential){
        
        boolean insert = false;
        if(credential.getId() == null){
            insert = true;
        }

        Optional<Credentials> target = Optional.ofNullable(mapper.selectCredentials(credential.getId()));
        if(target.isEmpty()){
            insert = true;
        }

        if(insert){
            mapper.insertCredentials(credential);
        }else{
            mapper.updateCredentials(target.get());
        }

    }
    public void saveCredentialWithRepo(Credentials credential){
        Optional<Credentials> target = repo.findById(credential.getId());
    
        if(target.isPresent()){
            BeanUtils.copyProperties(credential, target.get(), "id");
            repo.save(target.get());
        }else{
            repo.save(credential);
        }
    }
}
