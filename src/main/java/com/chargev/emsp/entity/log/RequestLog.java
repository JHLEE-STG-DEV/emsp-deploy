package com.chargev.emsp.entity.log;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "LOGS")
@Entity
@Data
public class RequestLog {
    @Id
    @Column(name = "LOG_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String logId;
    
    @Column(name = "REQUEST_URL", columnDefinition = "VARCHAR(2048)")
    private String requestUrl;
    @Column(name = "REQUEST_BODY", columnDefinition = "MEDIUMTEXT")
    private String requestBody;

    @CreationTimestamp
    @Column(name = "START_DATE", columnDefinition = "DATETIME", nullable = false)
    private Date  startDate;
    
    @Column(name = "END_DATE", columnDefinition = "DATETIME")
    private Date  endDate;

    @Column(name = "END_STATUS", columnDefinition = "INT")
    private int status;
    
    @Column(name = "RESULT_CODE", columnDefinition = "VARCHAR(255)")
    private String resultCode;
    @Column(name = "RESULT_MESSAGE", columnDefinition = "VARCHAR(2048)")
    private String resultMessage;
    @Column(name = "RESULT_BODY", columnDefinition = "MEDIUMTEXT")
    private String resultBody;
}
