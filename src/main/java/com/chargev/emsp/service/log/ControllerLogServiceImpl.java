package com.chargev.emsp.service.log;

import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.log.CheckpointLog;
import com.chargev.emsp.entity.log.RequestLog;
import com.chargev.emsp.repository.log.CheckpointLogRepository;
import com.chargev.emsp.repository.log.RequestLogRepository;
import com.chargev.utils.IdHelper;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ControllerLogServiceImpl implements ControllerLogService {

    private final ObjectMapper objectMapper;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private final RequestLogRepository logRepository;
    private final CheckpointLogRepository checkpointLogRepository;
    private static final Logger logger = LoggerFactory.getLogger(PncLogServiceImpl.class);

    private boolean useDbLog() {
        // 나중에 안쓰고싶을지도
        return true;
    }

    @Override
    public String controllerLogStart(String requestUrl) {
        String trackId = IdHelper.genLowerUUID32();
        // 로컬에 기록
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: ENDPOINT_START, Request URL: {}, Track ID: {}", requestUrl, trackId);
        }

        if (useDbLog()) {
            RequestLog log = new RequestLog();
            log.setLogId(trackId);
            log.setRequestUrl(requestUrl);
            log.setStartDate(new Date());

            try {
                logRepository.save(log);
            } catch (Exception ex) {
                logger.error("controllerLogStart : DB 저장 실패.");
            }
        }

        return trackId;
    }

    @Override
    public String controllerLogStart(String requestUrl, Object request) {
        String trackId = IdHelper.genLowerUUID32();
        String bodyJson = null;
        try {
            bodyJson = objectMapper.writeValueAsString(request);
        } catch (Exception ex) {
            logger.error("controllerLogStart Body 직렬화 실패.");
        }

        // 로컬에 기록
        if (apiLogger.isInfoEnabled()) {
            if (bodyJson == null) {
                apiLogger.info("TAG: ENDPOINT_START, Request URL: {}, Track ID: {}", requestUrl, trackId);
            } else {
                apiLogger.info("TAG: ENDPOINT_START, Request URL: {}, Track ID: {}, Body: {}", requestUrl, trackId, bodyJson);
            }
        }
        if (useDbLog()) {
            RequestLog log = new RequestLog();
            log.setLogId(trackId);
            log.setRequestUrl(requestUrl);
            log.setResultBody(bodyJson);
            log.setStartDate(new Date());

            try {
                logRepository.save(log);
            } catch (Exception ex) {
                logger.error("controllerLogStart : DB 저장 실패.");
            }
        }

        return trackId;
    }

    @Override
    public String controllerLogCheckpoint(String logId, String tag) {
        // 로컬에 기록
        if (apiLogger.isInfoEnabled()) {
            apiLogger.info("TAG: {}, Track ID: {}", tag, logId);
        }
        if (useDbLog()) {
            CheckpointLog log = new CheckpointLog();
            log.setCheckpointId(IdHelper.genLowerUUID32());
            log.setLogId(logId);
            log.setCheckpointTag(tag);
            log.setCreatedDate(new Date());

            try {
                checkpointLogRepository.save(log);
            } catch (Exception ex) {
                logger.error("controllerLogCheckpoint : DB 저장 실패.");
            }
        }

        return logId;
    }

    @Override
    public String controllerLogEnd(String logId, boolean succeed, String resultCode) {
        controllerLogEnd(logId, succeed, resultCode, null, null);

        return logId;
    }

    @Override
    public String controllerLogEnd(String logId, boolean succeed, String resultCode, String resultMessage) {
        controllerLogEnd(logId, succeed, resultCode, resultMessage, null);

        return logId;
    }

    @Override
    public String controllerLogEnd(String logId, boolean succeed, String resultCode, Object resultBody) {
        controllerLogEnd(logId, succeed, resultCode, null, resultBody);

        return logId;
    }

    private void controllerLogEnd(String logId, boolean succeed, String resultCode, String resultMessage,
            Object resultBody) {
        String bodyJson = null;
        try {
            if (resultBody != null)
                bodyJson = objectMapper.writeValueAsString(resultBody);
        } catch (Exception ex) {
            logger.error("controllerLogEnd Body 직렬화 실패.");
        }
        // 로컬에 기록
        if (succeed) {
            if (apiLogger.isInfoEnabled()) {
                if (bodyJson == null) {
                    apiLogger.info("TAG: ENDPOINT_END_SUCCESS, Track ID: {}, Result Code: {}, Result Message: {}", logId,
                            resultCode, resultMessage);

                } else if (resultMessage == null) {
                    apiLogger.info("TAG: ENDPOINT_END_SUCCESS, Track ID: {}, Result Code: {}, Result Body: {}", logId,
                            resultCode, bodyJson);
                } else {
                    apiLogger.info(
                            "TAG: ENDPOINT_END_SUCCESS, Track ID: {}, Result Code: {}, Result Message: {}, Result Body: {}",
                            logId, resultCode, resultMessage, bodyJson);
                }
            }
        } else {
            // 실패는 warn으로 넣어두자.
            if (apiLogger.isWarnEnabled()) {
                apiLogger.warn("TAG: ENDPOINT_END_FAIL, Track ID: {}, Result Code: {}, Result Message: {}", logId, resultCode,
                        resultMessage);
            }
        }
        if (useDbLog()) {
            Optional<RequestLog> log = Optional.empty();
            try {
                log = logRepository.findById(logId);
            } catch (Exception ex) {
                logger.error("controllerLogEnd : DB 조회 실패.");
            }

            if (log.isPresent()) {
                log.get().setEndDate(new Date());
                log.get().setResultCode(resultCode);
                if (resultMessage != null) {
                    log.get().setResultMessage(resultMessage);
                }
                if (bodyJson != null) {
                    log.get().setResultBody(bodyJson);

                }
                if (succeed) {
                    log.get().setStatus(1);
                } else {
                    log.get().setStatus(2);
                }
                try {
                    logRepository.saveAndFlush(log.get());
                } catch (Exception ex) {
                    logger.error("controllerLogEnd : DB 저장 실패.");
                }
            }
        }
    }

}
