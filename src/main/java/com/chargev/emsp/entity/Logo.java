package com.chargev.emsp.entity;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Logo {

    @Id
    @GenericGenerator(name = "uuidWithNoHyphen", strategy = "com.chargev.emsp.entity.idgen.IdGenerator")
    @GeneratedValue(generator = "uuidWithNoHyphen")
    private String id;

    private String url;
    private String thumbnail;
    private String category;
    private String type;
    private int width;
    private int height;

}