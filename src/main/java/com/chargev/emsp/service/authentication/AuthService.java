package com.chargev.emsp.service.authentication;

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
        if(tokenIssueHistory.getIssueSerial() == 0) {
            
        } else {
            tokenIssueHistory.setIssueSerial(tokenIssueHistory.getIssueSerial() + 1);
        }

        return tokenIssueHistoryRepository.save(tokenIssueHistory);
    }   
}
