package com.chargev.emsp.service.log;

import lombok.Data;

@Data
public class CheckpointReference {
    private CheckpointKind checkpointKind;
    private String refId;

    public CheckpointReference(String refId){
        this.refId = refId;
    }
}
