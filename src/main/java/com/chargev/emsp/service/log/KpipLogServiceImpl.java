package com.chargev.emsp.service.log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.log.KpipRequestLog;
import com.chargev.emsp.repository.log.KpipRequsetLogRepository;
import com.chargev.utils.IdHelper;
import com.chargev.utils.JsonHelper;
import com.chargev.utils.LocalFileManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KpipLogServiceImpl implements KpipLogService{

    private boolean backupLogActive = true;
    private boolean objectLogActive = true;

    private Path tmpLogPath;
    private Path objectLogPath;

    private final KpipRequsetLogRepository logRepository;
    private final JsonHelper jsonHelper;

    private static final Logger logger = LoggerFactory.getLogger(KpipLogServiceImpl.class);

    
    @PostConstruct
    public void init() {
        // JAR 파일 기준으로 logs 디렉토리 설정
        // logs 디렉토리가 존재하지 않으면 생성
        Path logDirectoryPath;
        try {
            
            Path logRootDir = Paths.get("/var/log/chargeV");
            //Path jarDir = Paths.get(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            logDirectoryPath = logRootDir.resolve("kpip");
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
            logger.error("Kpip 로그 백업플랜이 설정되지 않음.");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
        try {
            objectLogPath = logDirectoryPath.resolve("objects");
            LocalFileManager.ensureDirectory(objectLogPath);
        } catch (IOException e) {
            objectLogActive = false;
            logger.error("Kpip 객체 저장플랜이 설정되지 않음.");
            logger.error(e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public String kpipLogStart(String endpoint, Object request, String trackId) {
        KpipRequestLog log = new KpipRequestLog();
        String logKey = IdHelper.genLowerUUID32();


        log.setLogId(logKey);
        log.setEndpoint(endpoint);
        log.setTrackId(trackId);
        // Annotation이 자동으로 넣어줄까? 일단 로컬백업플랜도 있으니 셋팅해보자.
        log.setStartDate(LocalDateTime.now());


        
        boolean logOnDb = true;

        try {
            logRepository.save(log);
        } catch (Exception ex) {
            logger.error("kpipLogStart : DB 저장 실패.");

            // 실패시 포기한다 vs 로컬에라도 저장해서 둔다.
            logOnDb = false;
        }

        if (!logOnDb && backupLogActive) {
            // 비상용 로컬저장
            try {
                String logJson = jsonHelper.objectToString(log);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(logKey));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("kpipLogStart 백업플랜 실패.");
                return null;
            }
        }

        // request를 object로 저장함.
        if (objectLogActive && request != null) {
            try {
                String requestJson = jsonHelper.objectToString(request);
                LocalFileManager.writeToFile(requestJson, objectLogPath.resolve(logKey + "_request"));
            } catch (Exception ex) {

                // 실패
                ex.printStackTrace();
                logger.error("kpipLogStart Object 저장 실패.");
            }
        }

        return logKey;
    }

    @Override
    public String kpipLogFinish(String id, String status, Object response) {
         // 이건 존재를 확인해야 한다.
        if (id == null) {
            return null;
        }
        Optional<KpipRequestLog> log = Optional.empty();
        try {
            log = logRepository.findById(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        boolean isBackupData = false;
        // 백업플랜일수도 있다.
        if (log.isEmpty() && backupLogActive) {
            // 비상용 로그가 있는지 확인
            try {
                String logJson = LocalFileManager.readFromFile(tmpLogPath.resolve(id));
                KpipRequestLog logDeserialized = jsonHelper.stringToObject(logJson, KpipRequestLog.class);
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
            logger.error("kpipLogFinish에서 " + id + "에 대응되는 로그를 찾지 못함");
            return null;
        }
        
        KpipRequestLog logEntity = log.get();
        // 종료시점 저장
        logEntity.setEndDate(LocalDateTime.now());
        logEntity.setStatus(status);

        // 저장
        if (isBackupData) {
            try {
                String logJson = jsonHelper.objectToString(logEntity);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(id));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("kpipLogFinish 백업플랜 실패. : " + id);
            }
        }

        // start는 백업이었을지라도, 한번 이번에는 되는지 DB에도 찔러본다.

        try {
            logRepository.save(logEntity);
        } catch (Exception ex) {
            logger.error("kpipLogFinish : DB 저장 실패 : " + id);
        }

        // response를 object로 저장함.
        if (objectLogActive && response != null) {
            try {
                String responseJson = jsonHelper.objectToString(response);
                LocalFileManager.writeToFile(responseJson, objectLogPath.resolve(id + "_response"));
            } catch (Exception ex) {

                // 실패
                ex.printStackTrace();
                logger.error("kpipLogFinish Object 저장 실패.");
            }
        }

        return id;
    }

    @Override
    public String kpipLogFail(String id, String errorCode, String errorMessage) {
         // 이건 존재를 확인해야 한다.
        if (id == null) {
            return null;
        }
        Optional<KpipRequestLog> log = Optional.empty();
        try {
            log = logRepository.findById(id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        boolean isBackupData = false;
        // 백업플랜일수도 있다.
        if (log.isEmpty() && backupLogActive) {
            // 비상용 로그가 있는지 확인
            try {
                String logJson = LocalFileManager.readFromFile(tmpLogPath.resolve(id));
                KpipRequestLog logDeserialized = jsonHelper.stringToObject(logJson, KpipRequestLog.class);
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
            logger.error("kpipLogFail " + id + "에 대응되는 로그를 찾지 못함");
            return null;
        }

        KpipRequestLog logEntity = log.get();
        // 실패정보 저장
        logEntity.setEndDate(LocalDateTime.now());
        logEntity.setStatus("ERROR");
        logEntity.setErrorCode(errorCode);
        logEntity.setErrorMessage(errorMessage);

        // 저장
        if (isBackupData) {
            try {
                String logJson = jsonHelper.objectToString(logEntity);
                LocalFileManager.writeToFile(logJson, tmpLogPath.resolve(id));
            } catch (Exception ex) {
                // 여기서도 안되면 그냥 실패
                ex.printStackTrace();
                logger.error("kpipLogFail 백업플랜 실패. : " + id);
            }
        }

        // start는 백업이었을지라도, 한번 이번에는 되는지 DB에도 찔러본다.

        try {
            logRepository.save(logEntity);
        } catch (Exception ex) {
            logger.error("kpipLogFail : DB 저장 실패 : " + id);
        }

        return id;
    }
    
}
