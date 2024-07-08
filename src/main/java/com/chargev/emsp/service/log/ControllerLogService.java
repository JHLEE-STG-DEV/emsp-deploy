package com.chargev.emsp.service.log;

public interface ControllerLogService {
    public String controllerLogStart(String requestUrl);
    public String controllerLogStart(String requestUrl, Object request);

    public String controllerLogCheckpoint(String logId, String tag);

    public String controllerLogEnd(String logId, boolean succeed, String resultCode);
    public String controllerLogEnd(String logId, boolean succeed, String resultCode, String resultMessage);
    public String controllerLogEnd(String logId, boolean succeed, String resultCode, Object resultBody);
}
