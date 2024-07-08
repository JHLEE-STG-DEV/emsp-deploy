package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CheckRequest {
    @Schema(description = "체크용 Id. 202리턴 시 Body에 담겨져있는 값.")
    private String id;
    
}
