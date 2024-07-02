package com.chargev.emsp.entity.cert;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_CERTIFICATION")
@Entity
@Data
public class Certification {
    @Id
    @Column(name = "CERT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String certId;
    


    // 설명이랑 리퀘스트가 다르다. => 인증서를 해시해서 보내는게 아니다. 본인들의 정보를 해시해서 보낸다.
    @Column(name= "EC_KEY", columnDefinition = "BIGINT")
    private Long ecKey;

    // 이건 leaf다. 차후 대비를 위해 sub를 참조걸어둔다.
    @Column(name="SUB_CERT_ID", columnDefinition="CHAR(32)")
    private String subCertId;

    
    @Column(name = "ISSUER_NAME", columnDefinition = "VARCHAR(1024)")
    private String issuerName;
    @Column(name = "ISSUER_KEY", columnDefinition = "VARCHAR(1024)")
    private String issuerKey;
    @Column(name="SERIAL_NUMBER", columnDefinition = "VARCHAR(40)")
    private String serialNumber;




    // 0 : 정상, 1: Expired, 2: Revoked
    @Column(name = "STATUS", columnDefinition = "INT")
    private int status;

    
    @Column(name = "CREATED_DATE", columnDefinition = "DATETIME")
    private Date createdDate;
    @Column(name = "EXPIRE_DATE", columnDefinition = "DATETIME")
    private Date expireDate;
    @Column(name = "REVOKED_DATE", columnDefinition = "DATETIME")
    private Date revokedDate;

}
