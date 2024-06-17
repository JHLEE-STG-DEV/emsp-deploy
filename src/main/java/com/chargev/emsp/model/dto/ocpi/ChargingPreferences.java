package com.chargev.emsp.model.dto.ocpi;
import lombok.Data;

@Data
public class ChargingPreferences {
    private String profile_type;
    private String departure_time;
    private int energy_need;
    private boolean discharge_allowed;
}
