package com.chargev.emsp.service.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.util.Streamable;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.entity.authenticationentity.Permission;
import com.chargev.emsp.entity.authenticationentity.PermissionBase;
import com.chargev.emsp.entity.authenticationentity.PermissionGroup;
import com.chargev.emsp.entity.authenticationentity.TokenIssueHistory;
import com.chargev.emsp.repository.authentication.AuthSubjectRepository;
import com.chargev.emsp.repository.authentication.PermissionBaseRepository;
import com.chargev.emsp.repository.authentication.PermissionGroupRepository;
import com.chargev.emsp.repository.authentication.PermissionRepository;
import com.chargev.emsp.repository.authentication.TokenIssueHistoryRepository;
import com.chargev.emsp.service.cryptography.SHAService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthSubjectRepository authSubjectRepository;
    private final TokenIssueHistoryRepository tokenIssueHistoryRepository;
    private final PermissionBaseRepository permissionBaseRepository;
    private final PermissionGroupRepository permissionGroupRepository;
    private final PermissionRepository permissionRepository;
    private final SHAService shaService;

    private static final String SALT_STRING = "1577d9c941ad49008f4161ad02728dd2";

    public List<PermissionBase> getPermissionByGroup(String groupId) {
        Iterable<PermissionBase> permissionBase = permissionGroupRepository.getPermissionByGroup(groupId);
        if(permissionBase != null) {
            return Streamable.of(permissionBase).toList();
        }
        else {
            return new ArrayList<>();
        }
    }

    public PermissionBase savePermissionBase(PermissionBase permissionBase) {
        return permissionBaseRepository.save(permissionBase);
    }

    public Permission savePermission(Permission permission) {
        return permissionRepository.save(permission);
    }

    public PermissionGroup savePermissionGroup(PermissionGroup permissionGroup) {
        return permissionGroupRepository.save(permissionGroup);
    }

    public AuthSubject getAuthSubject(String subjectId) {
        return authSubjectRepository.findById(subjectId).orElse(null);
    }

    public AuthSubject saveAuthSubject(AuthSubject authSubject) {
        return authSubjectRepository.save(authSubject);
    }

    public boolean deleteAuthSubject(String subjectId) {
        AuthSubject subject = authSubjectRepository.findById(subjectId).orElse(null);
        if(subject == null) {
            return false;
        }
        if(subject.getDeleted() == 1) {
            return true; // 이미 삭제된 상태
        }
        subject.setDeleted(1);
        authSubjectRepository.save(subject);

        subject = authSubjectRepository.findById(subjectId).orElse(null);
        if(subject == null) {
            return false;
        }
        return subject.getDeleted() == 1;
    }

    public TokenIssueHistory getTokenIssueHistory(String subjectId) {
        return tokenIssueHistoryRepository.findById(subjectId).orElse(null);
    }

    public AuthSubject loginWithPassword(String clientId, String clientSecret) {
        // 사용자 ID 비번 방식으로 로그인 처리
        AuthSubject subject = authSubjectRepository.findById(clientId).orElse(null);
        if(subject == null) {
            return null;
        }

        String hashedPassword = shaService.sha256Hash(clientSecret, SALT_STRING);

        if(!subject.getSubjectPassword().equals(hashedPassword)) {
            return null;
        }

        return subject;
    }

    @Transactional
    public TokenIssueHistory saveTokenIssueHistory(TokenIssueHistory tokenIssueHistory) {
        TokenIssueHistory tokenSaveResult = null;
        int errorCode = 0;
        if(tokenIssueHistory.getIssueSerial() == 0) {
            Integer maxIssueSerial = tokenIssueHistoryRepository.getNextSerial(tokenIssueHistory.getSubjectId()) + 1;
            tokenIssueHistory.setIssueSerial(maxIssueSerial);
            tokenIssueHistory.setIssueId(UUID.randomUUID().toString().replace("-", ""));
            try 
            {
                tokenSaveResult = tokenIssueHistoryRepository.save(tokenIssueHistory);
            }
            catch (DataIntegrityViolationException e) 
            {
                errorCode = 1;
            } 
            catch (JpaSystemException e) 
            {
                errorCode = 2;
            } 
            catch (Exception e) 
            {
                errorCode = -1;
            }
            if(errorCode > 0) {
                try {
                    // 스레드 경쟁으로 인해 해당 번호가 사라졌을 수 있으니 하나 더 증가시켜 처리해 봄 
                    // 해당 시리얼 입력에는 DB상에 UNIQUE 제약이 걸려 있어야 함 
                    tokenIssueHistory.setIssueSerial(maxIssueSerial + 1);
                    tokenSaveResult = tokenIssueHistoryRepository.save(tokenIssueHistory);
                }
                catch (Exception e) {

                }
            }
        } else {
            try {
                tokenSaveResult = tokenIssueHistoryRepository.save(tokenIssueHistory);
           }
            catch (Exception e) {
            }
        } 
        return tokenSaveResult;
    }

}
