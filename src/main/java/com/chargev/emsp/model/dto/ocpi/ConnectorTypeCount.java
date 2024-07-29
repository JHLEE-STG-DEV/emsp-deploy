package com.chargev.emsp.model.dto.ocpi;

import lombok.Data;

@Data
public class ConnectorTypeCount {
    private ConnectorType standard;
    private Number count;
}
