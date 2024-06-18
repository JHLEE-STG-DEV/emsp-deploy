package com.chargev.emsp.service.authentication;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.authenticationentity.AuthSubject;
import com.chargev.emsp.entity.authenticationentity.TokenIssueHistory;
import com.chargev.emsp.repository.authentication.AuthSubjectRepository;
import com.chargev.emsp.repository.authentication.TokenIssueHistoryRepository;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthSubjectRepository authSubjectRepository;
    private final TokenIssueHistoryRepository tokenIssueHistoryRepository;

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

    @Transactional
    public TokenIssueHistory saveTokenIssueHistory(TokenIssueHistory tokenIssueHistory) {
        TokenIssueHistory tokenSaveResult = null;
        int errorCode = 0;
        if(tokenIssueHistory.getIssueSerial() == 0) {
            Integer maxIssueSerial = tokenIssueHistoryRepository.getNextSerial(tokenIssueHistory.getSubjectId()) + 1;
            tokenIssueHistory.setIssueSerial(maxIssueSerial);
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
