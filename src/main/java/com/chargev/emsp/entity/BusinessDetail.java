package com.chargev.emsp.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;


@Data
@Entity
public class BusinessDetail {
    @Id
    @GenericGenerator(name = "uuidWithNoHyphen", strategy = "com.chargev.emsp.entity.idgen.IdGenerator")
    @GeneratedValue(generator = "uuidWithNoHyphen")
    private String id;




    private String website;
    private String name;

    @JoinColumn(name = "logo_id")
    @OneToOne(fetch = FetchType.LAZY)
    private Logo logo;

}