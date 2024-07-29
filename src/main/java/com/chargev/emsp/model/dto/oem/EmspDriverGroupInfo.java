package com.chargev.emsp.model.dto.oem;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmspDriverGroupInfo {
    @Schema(description = "Driver Group Id")
    private String id;

    @Schema(description = "Driver Group Description")
    private String description;
}
