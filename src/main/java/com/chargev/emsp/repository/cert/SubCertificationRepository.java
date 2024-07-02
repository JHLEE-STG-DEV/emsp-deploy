package com.chargev.emsp.repository.cert;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.cert.SubCertification;

public interface SubCertificationRepository extends JpaRepository<SubCertification, String>{
    
    // 메서드 이름을 기반으로 쿼리
    Optional<SubCertification> findByHashedCert(String hashedCert);
}
