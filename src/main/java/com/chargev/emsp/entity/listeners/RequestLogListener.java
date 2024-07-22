package com.chargev.emsp.entity.listeners;

import com.chargev.emsp.entity.log.RequestLog;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class RequestLogListener {
    
    @PrePersist
    @PreUpdate
    public void validateRequestLog(RequestLog log) {
        truncateFields(log);
    }

    private void truncateFields(RequestLog log) {
        log.setResultMessage(Truncater.truncateIfOver(log.getResultBody(), 2048));
        log.setRequestUrl(Truncater.truncateIfOver(log.getRequestUrl(), 2048));
    }
}
