package com.chargev.emsp.entity.oem;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "OEM_ACCOUNT")
@Data
@Entity
public class Account {
    @Id
    @Column(name = "EMSP_ACCOUNT_KEY", columnDefinition = "VARCHAR(255)", nullable = false)
    private String emspAccountKey;

    @Column(name = "ACCOUNT_STATUS", columnDefinition = "INT", nullable = false)
    private int accountStatus;
    
    @Column(name = "CIAM_ID", columnDefinition = "VARCHAR(255)", nullable = false)
    private String ciamId;

    @Column(name = "NAME", columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "EMAIL", columnDefinition = "VARCHAR(255)", nullable = false)
    private String email;

    @Column(name = "MOBILE_NUMBER", columnDefinition = "VARCHAR(20)")
    private String mobileNumber;

    @Column(name = "ZIP_CODE", columnDefinition = "VARCHAR(10)")
    private String zipCode;

    @Column(name = "STREET", columnDefinition = "VARCHAR(255)")
    private String street;

    @Column(name = "HOUSE_NUMBER", columnDefinition = "VARCHAR(20)")
    private String houseNumber;

    @Column(name = "CITY", columnDefinition = "VARCHAR(100)")
    private String city;

    @Column(name = "COUNTRY", columnDefinition = "VARCHAR(100)")
    private String country;

    @OneToMany(mappedBy = "account")
    private List<OemContract> contracts;
}
