package com.chargev.emsp.entity.keyentity;

import java.util.Date;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Table(name = "EV_AUTH_KEYS")
@Entity
@Data

public class Keys {
    @Id
    @Column(name = "KEY_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String keyId;

    @Column(name = "KEY_NAME", columnDefinition = "VARCHAR(32)", nullable = false)
    private String keyName;

    @Column(name = "KEY_TYPE", columnDefinition = "VARCHAR(3)", nullable = false)
    private String keyType; // EC, RSA, AES

    @Column(name = "KEY_STATUS", columnDefinition = "INT",  nullable = false)
    private int keyStatus;

    @Column(name = "KEY_DESC", columnDefinition = "VARCHAR(255)")
    private String keyDesc;

    @Column(name = "PRIVATE_KEY", columnDefinition = "VARCHAR(1024)", nullable = false)
    private String privateKey;

    @Column(name = "PUBLIC_KEY", columnDefinition = "VARCHAR(1024)")
    private String publicKey;

    @Column(name = "CREATED_DATE", columnDefinition = "DATE",  nullable = false)
    @CreationTimestamp
    private Date createdDate;

    @Column(name = "UPDATED_DATE", columnDefinition = "DATE")
    @CreationTimestamp
    private Date updatedDate;

    @Column(name = "CREATED_USER", columnDefinition = "CHAR(32)")
    private String createdUser;

    @Column(name = "UPDATED_USER", columnDefinition = "CHAR(32)")
    private String updatedUser;

    @Column(name = "CLIENT_ID", columnDefinition = "VARCHAR(32)")
    private String clientId;

    @Column(name = "CLIENT_SECRET", columnDefinition = "VARCHAR(32)")
    private String clientSecret;

    @Column(name = "DELETED", columnDefinition = "VARCHAR(32)")
    @ColumnDefault("0")
    private int deleted;

}
