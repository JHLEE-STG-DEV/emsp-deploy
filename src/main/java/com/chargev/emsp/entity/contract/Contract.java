package com.chargev.emsp.entity.contract;

import java.util.Date;

import com.chargev.emsp.entity.listeners.ContractListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_CONTRACT")
@Entity
@EntityListeners(ContractListener.class)
@Data
public class Contract {
    @Id
    @Column(name = "CONTRACT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String contractId;
    

    @Column(name = "EMA_BASE_NUMBER", nullable = false, unique = true, insertable = false, updatable = false)
    private int emaBaseNumber;

    @Column(name = "ISSUED", columnDefinition = "INTEGER")
    private int issued;
    @Column(name = "EMA_ID", columnDefinition = "VARCHAR(128)")
    private String emaId;

    @Column(name = "PCID", columnDefinition = "VARCHAR(128)")
    private String pcid;
    @Column(name = "OEM_ID", columnDefinition = "VARCHAR(128)")
    private String oemId;
    @Column(name = "MEMBER_KEY", columnDefinition = "BIGINT")
    private Long memberKey;
    @Column(name = "MEMBER_GROUP_ID", columnDefinition = "VARCHAR(128)")
    private String memberGroupId;
    @Column(name = "MEMBER_GROUP_SEQ", columnDefinition = "BIGINT")
    private Long memberGroupSeq;

    @Column(name = "WHITELISTED", columnDefinition = "INTEGER")
    private int whitelisted;

    
    @Column(name = "CONTRACT_START_DATE_STRING", columnDefinition = "VARCHAR(128)")
    private String contractStartDateString;

    
    @Column(name = "CONTRACT_END_DATE_STRING", columnDefinition = "VARCHAR(128)")
    private String contractEndDateString;

    
    // 0 : 정상, 1: Expired, 2: Revoked -1: 발급도 실패한 흔적기관
    @Column(name = "STATUS", columnDefinition = "INT")
    private int status;
    @Column(name = "STATUS_MESSAGE", columnDefinition = "VARCHAR(1024)")
    private String statusMessage;
    @Column(name = "REVOKED_DATE", columnDefinition = "DATETIME")
    private Date revokedDate;

    
    
    @Column(name= "FULL_CERT", columnDefinition = "MEDIUMTEXT")
    private String fullCert;
}
