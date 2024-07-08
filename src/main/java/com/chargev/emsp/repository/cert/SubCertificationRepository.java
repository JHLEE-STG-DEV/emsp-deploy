package com.chargev.emsp.repository.cert;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.cert.SubCertification;

public interface SubCertificationRepository extends JpaRepository<SubCertification, String>{
    
  
    @Query(value = "SELECT * FROM EV_SUB_CERTIFICATION  WHERE hashed_cert = :hashedCert LIMIT 1", nativeQuery = true)
    Optional<SubCertification> findFirstByHashedCert(@Param("hashedCert") String hashedCert);
    // 메서드 이름을 기반으로 쿼리
    // 이건 정말 유일성이 보장될때만 가능
    //Optional<SubCertification> findByHashedCert(String hashedCert);
}
