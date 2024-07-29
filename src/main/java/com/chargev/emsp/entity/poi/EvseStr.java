package com.chargev.emsp.entity.poi;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.ArrayList;

@Data
public class EvseStr {
    private String access;
    private String companyId;
    private String stationId;
    private Directions directions;
    private Date modifiedDt;
    private ArrayList<ConnectorStr> connectorStr;
    private String chargerNumber;
    private String chargerStatus;
    private String stationNumber;
    private String physicalReference;
}