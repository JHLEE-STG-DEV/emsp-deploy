package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class CommandReserveRequest {
    private String response_url;
    private Token token;
    private String expiry_date;
    private String reservation_id;
    private String location_id;
    private String evse_uid;
    private String authorization_reference;
}
