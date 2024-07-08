package com.chargev.logger;

import com.chargev.utils.JsonHelper;

public class LogHelper {
    public static String controllerEnterLog(String endpoint) {
        return endpoint + " 요청";
    }
    public static String controllerEnterLog(String requestUrl, String trackId) {
        return requestUrl + " 요청. trackId = " + trackId;
    }


    public static String controllerTrackLog(String endpoint, String trackId) {
        return endpoint + " 요청기록 시작. trackId = " + trackId;
    }

    public static String controllerBackgroundLog(String endpoint, String trackId) {
        return endpoint + " 요청작업 백그라운드에서 실행. " + " trackId = "
                + trackId;
    }
    public static String controllerResponseLog(String endpoint, String trackId, String resultCode) {
        return endpoint + " 응답리턴 " + resultCode + ". trackId = " + trackId;
    }

    public static String controllerResponseLog(String endpoint, String trackId, String resultCode, Object resultBody) {
        return endpoint + " 응답리턴 " + resultCode + ". Body : " + JsonHelper.objectToString(resultBody) + " trackId = "
                + trackId;
    }
}
