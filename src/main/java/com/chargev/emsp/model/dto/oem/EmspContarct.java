package com.chargev.emsp.model.dto.oem;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspContarct {
    @Schema(description = "contract id")
    private String contract_id;
    @Schema(description = "계약상태")
    private String contract_status;
    @Schema(description = "vin number")
    private String vin;
    @Schema(description = "계약 개시")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private ZonedDateTime contract_start_date;
    @Schema(description = "계약 만료")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private ZonedDateTime contract_end_date;
    @Schema(description = "패키지")
    private EmspServicePackage service_package;
    @Schema(description = "rfid card")
    private EmspRfidCard rfid_card;
}
