package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.RequestLog;

public interface RequestLogRepository extends JpaRepository<RequestLog, String>{
    
}
