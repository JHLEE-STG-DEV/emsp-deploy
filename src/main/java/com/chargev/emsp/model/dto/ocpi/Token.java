package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class Token {
    private String country_code;
    private String party_id;
    private String uid;
    private String type;
    private String contract_id;
    private String visual_number;
    private String issuer;
    private String group_id;
    private boolean valid;
    private String whitelist;
    private String language;
    private String default_profile_type;
    private EnergyContract energy_contract;
    private String last_updated;
}
