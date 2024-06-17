package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class CommandStartRequest {
    private String response_url;
    private Token token;
    private String location_id;
    private String evse_uid;
    private String connector_id;
    private String authorization_reference;
}
