package com.chargev.emsp.entity.cert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="EV_SUB_CERTIFICATION")
@Entity
@Data
// 아직 구체적으로 사용할 일은 없으니 id와 해시만 저장해놓고 내용자체는 필요하면 읽어와서 쓰자.
public class SubCertification {
    @Id
    @Column(name = "CERT_ID", columnDefinition = "CHAR(32)", nullable = false, unique = true)
    private String certId;

    @Column(name= "EC_KEY", columnDefinition = "BIGINT")
    private Long ecKey;
    
    @Column(name= "HASHED_CERT", columnDefinition = "VARCHAR(128)")
    private String hashedCert;
    
    @Column(name= "FULL_CERT", columnDefinition = "MEDIUMTEXT")
    private String fullCert;
    
}
