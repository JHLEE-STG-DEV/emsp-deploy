package com.chargev.emsp.service.log;

public interface KpipLogService {
    // 헤더도 추적해야할까? 공통으로 쓰는거보니 딱히 의미는 없어보인다.
    public String kpipLogStart(String endpoint, Object request, String trackId);
    public String kpipLogFinish(String id, String status, Object response);
    public String kpipLogFail(String id, String errorCode, String errorMessage);
}
