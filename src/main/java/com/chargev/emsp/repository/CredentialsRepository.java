package com.chargev.emsp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.Credentials;

public interface CredentialsRepository extends JpaRepository<Credentials, String>{
    Optional<Credentials> findByToken(String token);
}
