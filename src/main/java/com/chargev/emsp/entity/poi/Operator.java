package com.chargev.emsp.entity.poi;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Operator {
    private String id;
    private String logo;
    private String name;
    private String website;

    @JsonProperty("phone_number")
    private String phoneNumber;
}