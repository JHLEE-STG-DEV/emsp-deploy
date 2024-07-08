package com.chargev.emsp.entity.contract;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "EV_CONTRACT_IDENTITY")
@Entity
@Data
@IdClass(ContractIdentityUK.class)
public class ContractIdentity {
    @Id
    @Column(name = "PCID", columnDefinition = "VARCHAR(128)")
    private String pcid;

    @Id
    @Column(name = "MEMBER_KEY", columnDefinition = "BIGINT")
    private Long memberKey;
        
    @Column(name = "WORKING", columnDefinition = "INTEGER")
    private int working;
    @Column(name = "WORKED", columnDefinition = "INTEGER")
    private int worked;
    @Column(name="CONTRACT_ID", columnDefinition =  "CHAR(32)")
    private String contractId;
    @Column(name = "CREATED_DATE", columnDefinition = "DATETIME")
    private Date createdDate = new Date();
}
