package com.chargev.emsp.entity.oem;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EMSP_KEY_BASE")
@Data
@Entity
public class EmspKeyBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EMSP_ACCOUNT_ID", columnDefinition = "INT", unique = true, insertable = false, updatable = false)
    private int id;

    @Column(name = "EMSP_ACCOUNT_KEY", columnDefinition = "CHAR(32)", nullable = false)
    private String emspAccountKey;

    @Column(name = "SEQUENCE_HEX", columnDefinition = "CHAR(7)", unique = true)
    private String sequenceHex;
}
