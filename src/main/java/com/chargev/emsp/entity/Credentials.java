package com.chargev.emsp.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import lombok.Data;
@Data
@Entity
public class Credentials {

    @Id
    @GenericGenerator(name = "uuidWithNoHyphen", strategy = "com.chargev.emsp.entity.idgen.IdGenerator")
    @GeneratedValue(generator = "uuidWithNoHyphen")
    private String id;



    private String token;
    private String url;


    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "Credentials_Roles",
        joinColumns = @JoinColumn(name = "credentials_id"),
        inverseJoinColumns = @JoinColumn(name = "roles_id")
    )

    private List<Roles> roles = new ArrayList<>();

}