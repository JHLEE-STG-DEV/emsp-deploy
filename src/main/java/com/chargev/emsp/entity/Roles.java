package com.chargev.emsp.entity;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;



@Data
@Entity
public class Roles {


    @Id
    @GenericGenerator(name = "uuidWithNoHyphen", strategy = "com.chargev.emsp.entity.idgen.IdGenerator")
    @GeneratedValue(generator = "uuidWithNoHyphen")
    private String id;



    private String role;


    @JsonProperty("party_id")
    @Column(name = "party_id")
    private String partyId;


    @JsonProperty("country_code")
    @Column(name = "country_code")
    private String countryCode;

    @JsonProperty("business_details")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_details_id")
    private BusinessDetail businessDetails;

}