package com.chargev.emsp.model.dto.ocpi;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EVSE {
    @Schema(maxLength=36, description = "EVSE 식별키")
    private String uid;
    @Schema(maxLength=48, description = "CPO의 플랫폼(및 하위 운영자 플랫폼) 내에서 EVSE를 식별할 수 있는 eMI3 표준의 식별자입니다.")
    private String evse_id;
    private Status status;
    private List<StatusSchedule> status_schedule;
    private List<String> capabilities;
    private List<Connector> connectors;
    private String floor_level;
    private String coordinates;
    private String physical_reference;
    private List<DisplayText> directions;
    private List<String> parking_restrictions;
    private List<Image> images;
    private String last_updated;
}
