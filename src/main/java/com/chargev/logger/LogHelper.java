package com.chargev.logger;

import org.springframework.stereotype.Component;

import com.chargev.utils.JsonHelper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LogHelper {
    private final JsonHelper jsonHelper;

    public String controllerEnterLog(String endpoint) {
        return endpoint + " 요청";
    }
    public String controllerEnterLog(String requestUrl, String trackId) {
        return requestUrl + " 요청. trackId = " + trackId;
    }


    public String controllerTrackLog(String endpoint, String trackId) {
        return endpoint + " 요청기록 시작. trackId = " + trackId;
    }

    public String controllerBackgroundLog(String endpoint, String trackId) {
        return endpoint + " 요청작업 백그라운드에서 실행. " + " trackId = "
                + trackId;
    }
    public String controllerResponseLog(String endpoint, String trackId, String resultCode) {
        return endpoint + " 응답리턴 " + resultCode + ". trackId = " + trackId;
    }

    public String controllerResponseLog(String endpoint, String trackId, String resultCode, Object resultBody) {
        return endpoint + " 응답리턴 " + resultCode + ". Body : " + jsonHelper.objectToString(resultBody) + " trackId = "
                + trackId;
    }
}
