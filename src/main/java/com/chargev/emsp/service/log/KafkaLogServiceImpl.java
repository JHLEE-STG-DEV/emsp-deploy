package com.chargev.emsp.service.log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.log.KafkaRequestLog;
import com.chargev.emsp.repository.log.KafkaRequestLogRepository;
import com.chargev.utils.IdHelper;
import com.chargev.utils.JsonHelper;
import com.chargev.utils.LocalFileManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaLogServiceImpl implements KafkaLogService {

    private final ObjectMapper objectMapper;
    private final KafkaRequestLogRepository logRepository;

    private boolean backupLogActive = true;
    private boolean objectLogActive = true;

    private Path tmpLogPath;
    private Path objectLogPath;

    private static final Logger logger = LoggerFactory.getLogger(KafkaLogServiceImpl.class);

    @PostConstruct
    public void init() {
        // JAR 파일 기준으로 logs 디렉토리 설정
        // logs 디렉토리가 존재하지 않으면 생성
        Path logDirectoryPath;
        try {
            Path logRootDir = Paths.get("/var/log/chargeV");
            //Path jarDir = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            logDirectoryPath = logRootDir.resolve("kafka");
            LocalFileManager.ensureDirectory(logDirectoryPath);
        } catch (IOException  e) {
            backupLogActive = false;
            objectLogActive = false;
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }
        try {
            tmpLogPath = logDirectoryPath.resolve("tmp");
            LocalFileManager.ensureDirectory(tmpLogPath);

        } catch (IOException e) {
            backupLogActive = false;
            logger.error("Kafka 로그 백업플랜이 설정되지 않음.");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        try {
            objectLogPath = logDirectoryPath.resolve("objects");
            LocalFileManager.ensureDirectory(objectLogPath);
        } catch (IOException e) {
            objectLogActive = false;
            logger.error("Kafka 객체 저장플랜이 설정되지 않음.");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public String kafkaLogStart(String topic, Object data, String trackId) {
        KafkaRequestLog log = new KafkaRequestLog();

        String logKey = IdHelper.genLowerUUID32();


        log.setLogId(logKey);
        log.setTopic(topic);
        log.setTrackId(trackId);
        log.setStartDate(LocalDateTime.now());

        boolean logOnDb = true;
        try {
            logRepository.save(log);
        } catch (Exception ex) {
            logger.error("kafkaLogStart : DB 저장 실패.");

            // 실패시 포기한다 vs 로컬에라도 저장해서 둔다.
            logOnDb = false;
        }
        if (!logOnDb && backupLogActive) {
            // 비상용 로컬저장
            try {
                String logJson = JsonHelper.objectToString(log);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(logKey));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("kafkaLogStart 백업플랜 실패.");
                return null;
            }
        }
        // data object로 저장함.
        if (objectLogActive && data != null) {
            try {
                String dataJson = JsonHelper.objectToString(data);
                LocalFileManager.writeToFile(dataJson, objectLogPath.resolve(logKey + "_data"));
            } catch (Exception ex) {
                // 실패
                ex.printStackTrace();
                logger.error("pncLogStart Object 저장 실패.");
            }
        }
        return logKey;
    }

    @Override
    public String kafkaFinish(String logId, Long offset) { // 이건 존재를 확인해야 한다.
        if (logId == null) {
            return null;
        }
        Optional<KafkaRequestLog> log = Optional.empty();
        try {
            log = logRepository.findById(logId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }  boolean isBackupData = false;
        // 백업플랜일수도 있다.
        if (log.isEmpty() && backupLogActive) {
            // 비상용 로그가 있는지 확인
            try {
                String logJson = LocalFileManager.readFromFile(tmpLogPath.resolve(logId));
                KafkaRequestLog logDeserialized = JsonHelper.stringToObject(logJson, KafkaRequestLog.class);
                if (logDeserialized != null) {
                    log = Optional.of(logDeserialized);
                    isBackupData = true;
                }
            } catch (Exception ex) {
                // 여기서도 없으면 없는것이지만 밖에서 한번에 리턴취급한다.
                ex.printStackTrace();
            }
        }

        // 여기서 없으면 없는거다.
        if (log.isEmpty()) {
            logger.error("kafkaFinish에서 " + logId + "에 대응되는 로그를 찾지 못함");
            return null;
        }

        KafkaRequestLog logEntity = log.get();
        // 종료시점 저장
        logEntity.setEndDate(LocalDateTime.now());
        logEntity.setStatus("FINISHED");
        logEntity.setOffset(offset);

        // 저장
        if (isBackupData) {
            try {
                String logJson = JsonHelper.objectToString(logEntity);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(logId));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("pncLogFinish 백업플랜 실패. : " + logId);
            }
        }

        // start는 백업이었을지라도, 한번 이번에는 되는지 DB에도 찔러본다.

        try {
            logRepository.save(logEntity);
        } catch (Exception ex) {
            logger.error("pncLogFinish : DB 저장 실패 : " + logId);
        }

        return logId;
    }

    @Override
    public String kafakaFail(String logId, String errorCode, String errorMessage) { // 이건 존재를 확인해야 한다.
        if (logId == null) {
            return null;
        }
        Optional<KafkaRequestLog> log = Optional.empty();
        try {
            log = logRepository.findById(logId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        boolean isBackupData = false;
        // 백업플랜일수도 있다.
        if (log.isEmpty() && backupLogActive) {
            // 비상용 로그가 있는지 확인
            try {
                String logJson = LocalFileManager.readFromFile(tmpLogPath.resolve(logId));
                KafkaRequestLog logDeserialized = JsonHelper.stringToObject(logJson, KafkaRequestLog.class);
                if (logDeserialized != null) {
                    log = Optional.of(logDeserialized);
                    isBackupData = true;
                }
            } catch (Exception ex) {
                // 여기서도 없으면 없는것이지만 밖에서 한번에 리턴취급한다.
                ex.printStackTrace();
            }
        }

        // 여기서 없으면 없는거다.
        if (log.isEmpty()) {
            logger.error("pncLogFail " + logId + "에 대응되는 로그를 찾지 못함");
            return null;
        }

        KafkaRequestLog logEntity = log.get();
        // 실패정보 저장
        logEntity.setEndDate(LocalDateTime.now());
        logEntity.setStatus("ERROR");
        logEntity.setErrorCode(errorCode);
        logEntity.setErrorMessage(errorMessage);

        // 저장
        if (isBackupData) {
            try {
                String logJson = JsonHelper.objectToString(logEntity);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(logId));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("pncLogFail 백업플랜 실패. : " + logId);
            }
        }

        // start는 백업이었을지라도, 한번 이번에는 되는지 DB에도 찔러본다.

        try {
            logRepository.save(logEntity);
        } catch (Exception ex) {
            logger.error("pncLogFail : DB 저장 실패 : " + logId);
        }

        return logId;
    }

}
