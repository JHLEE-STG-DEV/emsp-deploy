package com.chargev.emsp.entity.poi;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

@Data
public class ConnectorStr {
    private String voltage;
    private String amperage;
    private String tariffId;
    private Date modifiedDt;
    private int connectorId;
    private String connectorCode;
    private String connectorType;
    private String hourlyPowerChargeFilling;
}