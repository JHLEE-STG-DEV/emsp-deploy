package com.chargev.emsp.entity.oem;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EMSP_RFID")
@Data
@Entity
public class EmspRfidEntity {
    @Id
    @Column(name = "RFID_ID", columnDefinition = "CHAR(32)", nullable = false)
    private String id;

    @Column(name = "RFID_NUM", columnDefinition = "CHAR(16)", nullable = false)
    private String rfNum;

    // 카드의 상태
    @Column(name = "RFID_STATUS", columnDefinition = "INTEGER")
    private int status;

    // 상태 사유
    @Column(name = "RFID_STATUS_REASON", columnDefinition = "VARCHAR(255)")
    private String statusReason;

    // 카드 등록 일시
    @Column(name = "RFID_REGISTRATION_DATE", columnDefinition = "TIMESTAMP")
    private Date registrationDate;

    // 연동된 CONTRACT ID (JOIN으로 구성하지 않고 반정규화로 구성함)
    @Column(name = "CONTRACT_ID", columnDefinition = "CHAR(32)")
    private String contractId;

    // 여기서부터는 CONTRACT 테이블에 복사 뜨지 않는 항목들 (관리를 위한 항목)

    // 마지막 카드 사용(충전) 시작 시점
    @Column(name = "RFID_INUSE_AT", columnDefinition = "TIMESTAMP")
    private Date inUseAt;

    // 최근 업데이트 날짜 (관리상 필요할지도 몰라 우선 만들어둠)
    @Column(name = "RFID_UPDATED_AT", columnDefinition = "TIMESTAMP")
    private Date updatedAt;
}