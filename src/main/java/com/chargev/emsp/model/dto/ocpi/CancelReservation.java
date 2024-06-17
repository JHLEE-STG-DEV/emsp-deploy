package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CancelReservation {
    @Schema(maxLength=255, required=true, description ="CommandResult POST를 보내야 하는 URL입니다. 이 URL에는 CancelReservation 요청을 구분할 수 있는 고유 ID가 포함될 수 있습니다.")
    private String response_url;
    @Schema(maxLength=36, required=true, description ="이 예약에 대해 고유한 예약 ID입니다. Charge Point에 이 reservationId와 일치하는 예약이 이미 있는 경우 Charge Point가 예약을 대체합니다.")
    private String reservation_id;
}
