package com.chargev.emsp.repository.log;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chargev.emsp.entity.log.CpoRequestLog;

public interface CpoRequestLogRepository extends JpaRepository<CpoRequestLog, String> {

}