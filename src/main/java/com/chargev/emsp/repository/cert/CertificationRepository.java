package com.chargev.emsp.repository.cert;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.cert.Certification;

public interface CertificationRepository extends JpaRepository<Certification,String> {
        // 메서드 이름을 기반으로 쿼리 생성
        List<Certification> findByEcKeyAndStatus(Long ecKey, int status);
}
