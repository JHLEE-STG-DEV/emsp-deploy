package com.chargev.emsp.repository.cert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.chargev.emsp.entity.cert.Certification;      

public interface CertificationRepository extends JpaRepository<Certification,String> {
        // 메서드 이름을 기반으로 쿼리 생성
        @Query("SELECT c FROM Certification c WHERE c.ecKey = :ecKey AND c.status = :status")
    List<Certification> findByEcKeyAndStatus(@Param("ecKey") Long ecKey, @Param("status") int status);
        //List<Certification> findByEcKeyAndStatus(Long ecKey, int status);
}
