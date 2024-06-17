package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class CommandStopRequest {
    private String response_url;
    private String session_id;
}
