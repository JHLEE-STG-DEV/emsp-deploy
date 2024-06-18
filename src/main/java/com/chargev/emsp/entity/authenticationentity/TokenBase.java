package com.chargev.emsp.entity.authenticationentity;

import lombok.Data;

@Data
public class TokenBase {
    private String iss;
    private String sub;
    private String iat;
    private String exp;
    private String reg;
    private String roles;
    private String sn;
}
