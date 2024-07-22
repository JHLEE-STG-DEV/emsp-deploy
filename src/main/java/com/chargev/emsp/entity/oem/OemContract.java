package com.chargev.emsp.entity.oem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "OEM_CONTRACT")
@Data
@Entity
public class OemContract {
    @Id
    @Column(name = "CONTRACT_ID", columnDefinition = "VARCHAR(255)")
    private String contractId;

    @ManyToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = false)
    private Account account;

    @Column(name = "VIN", columnDefinition = "VARCHAR(255)", nullable = false)
    private String vin;

    @Column(name = "VEHICLE_TYPE", columnDefinition = "VARCHAR(255)")
    private String vehicleType;

    @Column(name = "MODE_NAME", columnDefinition = "VARCHAR(255)")
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

    @OneToMany(mappedBy = "oemContract")
    private List<RFID> rfids;

    @Column(name = "CONTRACT_STATUS", columnDefinition = "INTEGER")
    private int contractStatus;

    @Column(name = "CONTRACT_STATUS_REASON", columnDefinition = "VARCHAR(255)")
    private String contractStatusReason;

    @Column(name = "CONTRACT_START_DATE", columnDefinition = "TIMESTAMP")
    private Date contractStartDate; // 계약 시작 날짜 필드 추가

    public void setRfid(RFID rfid) {
        if (this.rfids == null) {
            this.rfids = new ArrayList<>();
        }
        this.rfids.add(rfid);
        rfid.setOemContract(this); // RFID 객체에 OemContract 설정
    }
}
