package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class AccessControl {
    private String uid;
    private String type;
    private String visual_number;
    private String issuer;
    private String group_id;
}
