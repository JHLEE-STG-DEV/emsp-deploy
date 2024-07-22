package com.chargev.emsp.entity.cert;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_CERT_IDENTITY")
@Entity
@Data
public class CertIdentity {
    @Id
    @Column(name = "EC_KEY", columnDefinition = "BIGINT")
    private Long ecKey;
        
    @Column(name = "WORKING", columnDefinition = "INTEGER")
    private int working;
    @Column(name = "WORKED", columnDefinition = "INTEGER")
    private int worked;
    @Column(name="CERT_ID", columnDefinition =  "CHAR(32)")
    private String certId;
    @Column(name = "CREATED_DATE", columnDefinition = "DATETIME")
    private Date createdDate = new Date();
    
}
