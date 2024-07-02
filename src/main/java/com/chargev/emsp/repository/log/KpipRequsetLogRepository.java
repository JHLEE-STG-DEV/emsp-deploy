package com.chargev.emsp.repository.log;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.KpipRequestLog;

public interface KpipRequsetLogRepository  extends JpaRepository<KpipRequestLog, String>{
    
}
