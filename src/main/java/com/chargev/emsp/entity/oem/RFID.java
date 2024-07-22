package com.chargev.emsp.entity.oem;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "OEM_RFID")
@Data
@Entity
public class RFID {
    @Id
    @Column(name = "ID", columnDefinition = "VARCHAR(100)", nullable = false)
    private String id;

    @Column(name = "RF_NUM", columnDefinition = "VARCHAR(16)", nullable = false)
    private String rfNum;

    @Column(name = "CARD_NAME", columnDefinition = "VARCHAR(20)")
    private String cardName;

    @Column(name = "STATUS", columnDefinition = "INTEGER")
    private int status;

    @Column(name = "PROGRESS_USE", columnDefinition = "INTEGER")
    private int progressUse;

    @Column(name = "SEND_STATUS", columnDefinition = "INTEGER")
    private int sendStatus;

    @Column(name = "ISSUED_AT", columnDefinition = "TIMESTAMP")
    private Date issuedAt;

    @ManyToOne
    @JoinColumn(name = "CONTRACT_ID")
    private OemContract oemContract;
}