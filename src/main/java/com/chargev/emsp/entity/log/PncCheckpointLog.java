package com.chargev.emsp.entity.log;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Table(name = "pnc_checkpoint_logs ")
@Entity
@Getter
@Setter
public class PncCheckpointLog {
    @Id
    @Column(name = "CHECKPOINT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String checkpointId;

    // 연관된 (pnc)로그의 id
    @Column(name = "LOG_ID", columnDefinition = "CHAR(32)", nullable = true)
    private String logId;
    @Column(name = "CHECKPOINT_FLAG", columnDefinition = "VARCHAR(255)", nullable = false)
    private String checkpointFlag;

    @Column(name = "REFERENCE_ID", columnDefinition = "CHAR(32)")
    private String refId;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", columnDefinition = "DATETIME", nullable = false)
    private LocalDateTime createdDate;

}
