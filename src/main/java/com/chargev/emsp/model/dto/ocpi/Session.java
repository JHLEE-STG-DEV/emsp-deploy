package com.chargev.emsp.model.dto.ocpi;

import java.util.List;
import lombok.Data;

@Data
public class Session {
    private String country_code;
    private String party_id;
    private String id;
    private String start_date_time;
    private String end_date_time;
    private int kwh;
    private CdrToken cdr_token;
    private String auth_method;
    private String authorization_reference;
    private String location_id;
    private String evse_uid;
    private String connector_id;
    private String meter_id;
    private String currency;
    private List<ChargingPeriod> charging_periods;
    private Price total_cost;
    private String status;
    private String last_updated;
}
