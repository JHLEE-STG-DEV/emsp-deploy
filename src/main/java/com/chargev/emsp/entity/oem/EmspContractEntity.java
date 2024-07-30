package com.chargev.emsp.entity.oem;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EMSP_CONTRACT")
@Data
@Entity
public class EmspContractEntity {
    @Id
    @Column(name = "CONTRACT_ID", columnDefinition = "CHAR(14)")
    private String contractId;

    @ManyToOne
    @JoinColumn(name = "EMSP_ACCOUNT_KEY", nullable = false)
    private EmspAccountEntity account;

    @Column(name = "VEHICLE_VIN", columnDefinition = "VARCHAR(255)", nullable = false)
    private String vin;

    @Column(name = "VEHICLE_TYPE", columnDefinition = "VARCHAR(255)")
    private String vehicleType;

    @Column(name = "VEHICLE_MODE_NAME", columnDefinition = "VARCHAR(255)")
    private String modeName;

    @Column(name = "PACKAGE_ID", columnDefinition = "VARCHAR(255)")
    private String packageId;

    @Column(name = "PACKAGE_NAME", columnDefinition = "VARCHAR(255)")
    private String packageName;

    @Column(name = "PACKAGE_EXPIRATION_DATE", columnDefinition = "TIMESTAMP")
    private Date packageExpirationDate;

    @Column(name = "PAYMENT_ASSET_ID", columnDefinition = "VARCHAR(255)")
    private String paymentAssetId;

    @Column(name = "PAYMENT_EXPIRATION_DATE", columnDefinition = "TIMESTAMP")
    private Date paymentExpirationDate;

    @Column(name = "CONTRACT_STATUS", columnDefinition = "INTEGER")
    private int contractStatus;

    @Column(name = "CONTRACT_STATUS_REASON", columnDefinition = "VARCHAR(255)")
    private String contractStatusReason;

    @Column(name = "CONTRACT_START_DATE", columnDefinition = "TIMESTAMP")
    private Date contractStartDate; // 계약 시작 날짜 필드 추가

    // RFID 관련된 내용을 JOIN테이블로 구성하지 않고 반정규화로 최신 내용만 복사로 구성함
    @Column(name = "RFID_ID", columnDefinition = "CHAR(32)")
    private String rfidId;

    @Column(name = "RFID_NUM", columnDefinition = "CHAR(16)")
    private String rfidNum;

    // 카드의 상태
    @Column(name = "RFID_STATUS", columnDefinition = "INTEGER")
    private int rfidStatus;

    // 카드의 상태
    @Column(name = "RFID_STATUS_REASON", columnDefinition = "VARCHAR(255)")
    private String rfidStatusReason;

    // 카드 등록 일시
    @Column(name = "RFID_REGISTRATION_DATE", columnDefinition = "TIMESTAMP")
    private Date rfidRegistrationDate;

}
