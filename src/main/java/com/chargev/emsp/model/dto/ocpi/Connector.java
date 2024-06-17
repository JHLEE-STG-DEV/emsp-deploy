package com.chargev.emsp.model.dto.ocpi;

import java.util.List;
import lombok.Data;

@Data
public class Connector {
    private String id;
    private String standard;
    private String format;
    private String power_type;
    private Integer max_voltage;
    private Integer max_amperage;
    private Integer max_electric_power;
    private List<String> tariff_ids;
    private String terms_and_conditions;
    private String last_updated;
}
