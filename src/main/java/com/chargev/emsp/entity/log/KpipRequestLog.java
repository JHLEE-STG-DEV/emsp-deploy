package com.chargev.emsp.entity.log;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.chargev.emsp.entity.listeners.KpipRequestLogListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "kpip_request_logs ")
@Entity
@EntityListeners(KpipRequestLogListener.class)
@Data
public class KpipRequestLog {
     @Id
    @Column(name = "LOG_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String logId;
    

    @Column(name = "ENDPOINT", columnDefinition = "VARCHAR(2048)")
    private String endpoint;

    @CreationTimestamp
    @Column(name = "START_DATE", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime  startDate;
    
    @Column(name = "END_DATE", columnDefinition = "DATETIME")
    private LocalDateTime  endDate;

    @Column(name = "END_STATUS", columnDefinition = "VARCHAR(255)")
    private String status;
    
    @Column(name = "ERROR_CODE", columnDefinition = "VARCHAR(255)")
    private String errorCode;

    @Column(name = "ERROR_MESSAGE", columnDefinition = "VARCHAR(2048)")
    private String errorMessage;
    @Column(name = "TRACK_ID", columnDefinition = "CHAR(32)")
    private String trackId;
}
