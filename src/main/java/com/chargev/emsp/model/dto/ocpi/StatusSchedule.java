package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class StatusSchedule {
    private String period_begin;
    private String period_end;
    private String status;
}
