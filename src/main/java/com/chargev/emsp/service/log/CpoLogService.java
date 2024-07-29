package com.chargev.emsp.service.log;

public interface CpoLogService {
    public String cpoLogStart(String endpoint, Object request, String trackId);
    public String cpoLogFinish(String id, String status, Object response);
    public String cpoLogFail(String id, String errorCode, String errorMessage);
}