package com.chargev.emsp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.chargev.emsp.entity.Credentials;

@Mapper

public interface CredentialsMapper {



    Credentials selectCredentials(@Param("id") String id);

    Credentials selectCredentialsByToken(@Param("token") String id);


    void insertCredentials(Credentials credentials);



    void updateCredentials(Credentials credentials);



    void deleteCredentials(@Param("id") String id);

}