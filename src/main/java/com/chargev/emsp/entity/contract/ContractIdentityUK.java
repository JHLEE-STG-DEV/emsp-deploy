package com.chargev.emsp.entity.contract;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class ContractIdentityUK implements Serializable{
    private String pcid;

    private Long memberKey;

    // default constructor

}
