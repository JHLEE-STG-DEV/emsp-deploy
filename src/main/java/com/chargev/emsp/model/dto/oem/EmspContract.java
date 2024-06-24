package com.chargev.emsp.model.dto.oem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspContract {
    @Schema(description = "contract id")
    @JsonProperty("contract_id")
    private String contractId;
    @Schema(description = "계약상태")
    @JsonProperty("contract_status")
    private String contractStatus;
    @Schema(description = "vin number")
    private String vin;
    @Schema(description = "계약 개시")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    @JsonProperty("contract_start_date")
    private String contractStartDate;
    @Schema(description = "계약 만료")
    @JsonProperty("contract_end_date")
    @JsonFormat(pattern = "YYYY-MM-DDTHH:MM:SS.sssZ", timezone = "UTC")
    private String contractEndDate;
    @Schema(description = "패키지", type="EmspServicePackage")
    @JsonProperty("service_package")
    private EmspServicePackage servicePackage;
    @Schema(description = "rfid card", type = "EmspRfidCard")
    @JsonProperty("rfid_card")
    private EmspRfidCard rfidCard;
}
