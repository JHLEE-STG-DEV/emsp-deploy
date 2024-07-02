package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.PncRequestLog;

public interface PncRequestLogRepository extends JpaRepository<PncRequestLog, String>{
    
}
