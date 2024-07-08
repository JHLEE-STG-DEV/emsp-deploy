package com.chargev.emsp.entity.log;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "CHECKPOINT_LOGS")
@Entity
@Getter
@Setter
public class CheckpointLog {
    @Id
    @Column(name = "CHECKPOINT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String checkpointId;

    // 연관된 (pnc)로그의 id
    @Column(name = "LOG_ID", columnDefinition = "CHAR(32)", nullable = true)
    private String logId;
    @Column(name = "CHECKPOINT_TAG", columnDefinition = "VARCHAR(255)", nullable = false)
    private String checkpointTag;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", columnDefinition = "DATETIME", nullable = false)
    private Date createdDate;

    
}
