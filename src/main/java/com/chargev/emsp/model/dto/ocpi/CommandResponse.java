package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class CommandResponse {
    private String result;
    private int timeout;
    private Message message;
}
