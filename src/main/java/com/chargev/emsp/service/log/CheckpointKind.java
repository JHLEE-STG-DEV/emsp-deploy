package com.chargev.emsp.service.log;

public enum CheckpointKind {
    KAFKA_SEND_SUCCESS("KAFKA_SEND_SUCCESS"),
    KAFKA_SEND_FAIL("KAFKA_SEND_FAIL"),
    KPIP_SEND_SUCCESS("KPIP_SEND_SUCCESS"),
    KPIP_SEND_FAIL("KPIP_SEND_FAIL"),
    CPO_SEND_SUCCESS("CPO_SEND_SUCCESS"),
    CPO_SEND_FAIL("CPO_SEND_FAIL");

    private final String key;

    CheckpointKind(String key){
this.key = key;

}
public String key(){
    return key;
}
}
