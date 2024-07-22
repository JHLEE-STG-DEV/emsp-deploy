package com.chargev.emsp.repository.cert;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.cert.CertIdentity;

public interface CertIdentityRepository  extends JpaRepository<CertIdentity, Long>{
    
}
