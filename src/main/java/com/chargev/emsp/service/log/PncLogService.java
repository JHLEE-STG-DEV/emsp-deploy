package com.chargev.emsp.service.log;

public interface PncLogService {
    // TODO 요청에 대해 감당할 수 있는가? 일단 내부적으로 들고있되, 실사용시엔 제외하자.
    // 진입시 헤더를 생성
    public String pncLogStart(String endpoint, Object request);

    public String pncLogCheckpoint(String id, String checkpointKey, String refId);

    public String pncLogCheckpoint(String id, CheckpointReference checkpoint);

    public String pncLogFinish(String id);
    public String pncLogFinish(String id, String status);

    public String pncLogFinish(String id, String status, Object response);
    public String pncLogFail(String id, String errorCode, String errorMessage);

}
