package com.chargev.emsp.service.preview;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.chargev.emsp.entity.contract.Contract;
import com.chargev.emsp.entity.contract.ContractIdentity;
import com.chargev.emsp.entity.contract.ContractIdentityUK;
import com.chargev.emsp.model.dto.pnc.CertificateInfo;
import com.chargev.emsp.model.dto.pnc.ContractMeta;
import com.chargev.emsp.model.dto.pnc.ContractStatus;
import com.chargev.emsp.repository.contract.ContractIdentityRepository;
import com.chargev.emsp.repository.contract.ContractRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.utils.DateTimeFormatHelper;
import com.chargev.utils.IdHelper;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractManageService {
    private final ContractRepository contractRepository;
    private final ContractIdentityRepository contractIdRepository;
    private final CertificateConversionService conversionService;
    private static final Logger apiLogger = LoggerFactory.getLogger("API_LOGGER");
    private final EntityManager entityManager;

    public ServiceResult<CheckContractIssueCondition> checkCondition(String pcid, Long memberKey, String trackId,
            boolean requestNew) {
        ServiceResult<CheckContractIssueCondition> serviceResult = new ServiceResult<>();
        if (pcid == null || memberKey == null) {
            serviceResult.fail(404, "Bad Request");
            apiLogger.warn("Message: {}, Track ID: {}", "입력값이 Null입니다.", trackId);
            return serviceResult;
        }

        ContractReqType reqType = ContractReqType.FAIL;

        // 우선 사실상id가 존재하면 가져온다.
        ContractIdentityUK contractIdPair = new ContractIdentityUK(pcid, memberKey);
        Optional<ContractIdentity> contractIdentity = Optional.empty();
        try {
            ContractIdentity tryId = new ContractIdentity();
            tryId.setPcid(pcid);
            tryId.setMemberKey(memberKey);
            if (!contractIdRepository.existsById(contractIdPair)) {
                contractIdRepository.saveAndFlush(tryId);
            }
        } catch (Exception ex) {
            // 이미 존재할 수 있음. 여기선 개의치않음.
            ex.printStackTrace();
        }

        try {
            contractIdentity = contractIdRepository.findById(contractIdPair);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (contractIdentity.isEmpty()) {
            // 생성 자체에 실패했다.
            serviceResult.fail(500, "인증정보 생성에 실패하였습니다.");
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG: ISSUE_CONTRACT, MESSAGE: {} ,memberKey: {}, pcid: {} Track ID: {}",
                        "인증정보 조회에 실패하였습니다.", memberKey, pcid, trackId);
            }
            return serviceResult;
        }
        // 이 Identity를 이용해 조회시키도록 해도 되나?
        // 아니다. 풀링은 각 인증서별로 하는것이 좋다.
        ContractIdentity contractKey = contractIdentity.get();
        if (contractKey.getWorking() > 0 && contractKey.getWorked() == 0) {
            // 이미 발급절차중이다.
            // 그러면 그냥 동작중인 키를 주자. (working이면 거기에 대응되는 contractId가 존재한다고 가정)
            CheckContractIssueCondition issueCondition = new CheckContractIssueCondition();
            issueCondition.setContractId(contractKey.getContractId());
            issueCondition.setReqType(ContractReqType.WORKING);
            serviceResult.succeed(issueCondition);
            return serviceResult;
        }

        // isRequestNew 인데 contractId가 있으면? 중복이므로 FAIL
        // 뒤늦게 만료처리를 체크한번더? 아니면 신뢰?
        // 뒤늦게 체크를 한번 하는것으로 하자.
        String contractId = contractKey.getContractId();

        // work하고있지 않다. contract를 발급해둔다.
        Contract targetContract;
        if (contractId == null || !StringUtils.hasText(contractId)) {
            // 신규 emid생성
            contractId = IdHelper.genLowerUUID32();
            targetContract = new Contract();
            targetContract.setContractId(contractId);
            // DB에 저장
            try {
                targetContract = contractRepository.saveAndFlush(targetContract);
                // 엔티티를 다시 로드 (증가된 EMA_BASE_NUMBER를 바로 읽어오지 못하는 문제가 있어서 수정함)
                // entityManager.refresh(targetContract);

            } catch (Exception ex) {
                ex.printStackTrace();
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: ISSUE_CONTRACT, MESSAGE: {} ,memberKey: {}, pcid: {} Track ID: {}",
                            "CONTRACT 정보 테이블 저장 실패", memberKey, pcid, trackId);
                }
                serviceResult.fail(500, "Failed to INSERT DB");
            }

            // 일단 db에 저장을 쳐서 생성중절차를 밟게함.
            contractKey.setContractId(contractId);
            contractKey.setWorking(1);
            contractKey.setWorked(0);
            try {
                contractIdRepository.saveAndFlush(contractKey);
            } catch (Exception ex) {
                if (apiLogger.isErrorEnabled()) {
                    apiLogger.error("TAG: ISSUE_CONTRACT, MESSAGE: {} ,memberKey: {}, pcid: {} Track ID: {}",
                            "CONTRACT_IDENTITY 정보 WORKING플래그 작성 실패", memberKey, pcid, trackId);
                }
            }
            reqType = ContractReqType.NEW;
        } else {
            // 기존에 emid가 있다.
            reqType = ContractReqType.UPDATE;
        }
        try {
            targetContract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new RuntimeException("Contract not found"));

        } catch (Exception ex) {
            serviceResult.fail(400, "Failed to Query Contarct");
            return serviceResult;
        }

        // 관련 contract를 만들든 찾아오든 했다.
        // 이제 requestNew와 관련하여 조절한다.
        // 방금생성한거니까 체크할게 없다.
        if (requestNew) {

            // NEW는 DB에 방금생성한거니까 체크할게 없다.
            if (reqType.equals(ContractReqType.UPDATE) && targetContract.getEmaId() != null) {
                // 여기는 둘중하나다. 1. 살아있어서 중복처리거나 2. 죽어서 NEW거나
                if (isContractActive(targetContract)) {
                    // 살아있다. 이건 중복이다.
                    serviceResult.fail(409, "중복 가입");
                    return serviceResult;
                } else {
                    // 죽은놈이다. 이건 없애고, New인것처럼 행동해야한다.
                    // Flag로 죽은거니까, KEPKO에는 안쏴도되는거로 가정
                    contractId = IdHelper.genLowerUUID32();
                    targetContract = new Contract();
                    targetContract.setContractId(contractId);
                    // DB에 저장
                    try {
                        targetContract = contractRepository.saveAndFlush(targetContract);
                        // 엔티티를 다시 로드 (증가된 EMA_BASE_NUMBER를 바로 읽어오지 못하는 문제가 있어서 수정함)
                        // entityManager.refresh(targetContract);

                    } catch (Exception ex) {

                        ex.printStackTrace();
                        if (apiLogger.isErrorEnabled()) {
                            apiLogger.error("TAG: ISSUE_CONTRACT, MESSAGE: {} ,memberKey: {}, pcid: {} Track ID: {}",
                                    "CONTRACT 정보 테이블 저장 실패", memberKey, pcid, trackId);
                        }
                        serviceResult.fail(500, "Failed to INSERT DB");
                    }

                    // 일단 db에 저장을 쳐서 생성중절차를 밟게함.
                    contractKey.setContractId(contractId);
                    contractKey.setWorking(1);
                    contractKey.setWorked(0);
                    try {
                        contractIdRepository.saveAndFlush(contractKey);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        if (apiLogger.isErrorEnabled()) {
                            apiLogger.error("TAG: ISSUE_CONTRACT, MESSAGE: {} ,memberKey: {}, pcid: {} Track ID: {}",
                                    "CONTRACT_IDENTITY 정보 WORKING플래그 작성 실패", memberKey, pcid, trackId);
                        }
                    }
                    reqType = ContractReqType.NEW;
                }
            }

            // Add일수도 있다.
            if (hasCarFamily(pcid)) {
                reqType = ContractReqType.ADD;
            }
        } else if (reqType.equals(ContractReqType.NEW)) {
            // Contract 가 없는데 UPDATE로 요청한 것이다.
            serviceResult.fail(404, "업데이트할 인증서 정보가 존재하지 않습니다.");
            return serviceResult;
        }
        CheckContractIssueCondition issueCondition = new CheckContractIssueCondition();
        issueCondition.setContractId(targetContract.getContractId());
        issueCondition.setReqType(reqType);
        serviceResult.succeed(issueCondition);

        return serviceResult;
    }

    public ServiceResult<String> finishIssueContractIdentity(String pcid, Long memberKey, String trackId,
            boolean success, int errorCode, String message) {
        ServiceResult<String> result = new ServiceResult<>();
        // 우선 사실상id가 존재하면 가져온다.
        ContractIdentityUK contractIdPair = new ContractIdentityUK(pcid, memberKey);
        Optional<ContractIdentity> contractIdentity = Optional.empty();

        try {
            contractIdentity = contractIdRepository.findById(contractIdPair);
        } catch (Exception ex) {

        }
        if (contractIdentity.isEmpty()) {
            apiLogger.error("TAG:CONTRACT_FINISH_FAIL, MEMBERKEY: {}, PCID: {}, MESSAGE: {}, TRACKID: {}", memberKey,
                    pcid, "UNLOCK할 ContractId조회 불가", trackId);
        }
        ContractIdentity unlockedIdentity = contractIdentity.get();
        unlockedIdentity.setWorked(1);
        unlockedIdentity.setWorking(0);

        // Contract자체의 메세지도 여기서 남겨주자.

        // 근데 중복이면 건드리면안되고 그냥 공중분해시킨다.(저쪽도 중복여부는 추적하지않음.)
        if (errorCode == 409) {
            result.succeed("OK");
            return result;
        }

        Optional<Contract> realtedContract = Optional.empty();
        try {
            realtedContract = contractRepository.findById(unlockedIdentity.getContractId());
        } catch (Exception ex) {

        }
        if (realtedContract.isPresent()) {
            Contract relatedContractEntity = realtedContract.get();
            if (success) {
                relatedContractEntity.setStatus(0);
                relatedContractEntity.setStatusMessage("OK");
            } else {
                relatedContractEntity.setStatus(-1);
                relatedContractEntity.setStatusMessage(message);
            }
            try {
                contractRepository.save(relatedContractEntity);
            } catch (Exception ex) {
                apiLogger.error(
                        "TAG:CONTRACT_FINISH_FAIL, ContractId: {}, STATUS: {}, STATUS_MESSAGE: {}, MESSAGE: {}, TRACKID: {}",
                        unlockedIdentity.getContractId(), relatedContractEntity.getStatus(),
                        relatedContractEntity.getStatusMessage(), "성공여부를 저장 실패", trackId);
            }
        }
        try {
            contractIdRepository.save(unlockedIdentity);
        } catch (Exception ex) {
            apiLogger.error("TAG:CONTRACT_FINISH_FAIL, MEMBERKEY: {}, PCID: {}, MESSAGE: {}, TRACKID: {}",
                    memberKey, pcid, "UNLOCK 플래그 저장 실패", trackId);
        }

        result.succeed("OK");
        return result;
    }

    private boolean hasCarFamily(String pcid) {
        try {
            // 자기는 있을것이다. 하나 넘어야 Family
            List<ContractIdentity> targets = contractIdRepository.findAllByPcid(pcid);
            return targets.size() > 1;
        } catch (Exception ex) {
            // 모르겠다.
            return false;
        }

    }

    private boolean isContractActive(Contract contract) {
        if (contract == null)
            return false;

        if (contract.getStatus() != 0) {
            // 플래그까지 했으면 맞음.
            return false;
        }
        return true;

    }

    public ServiceResult<ContractMeta> findById(String contractId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();

        // DB에 저장
        try {
            Contract contract = contractRepository.findById(contractId).get();
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(400, "Failed to Query DB");
        }
        return result;
    }

    @Transactional
    public ServiceResult<ContractMeta> createEmptyContract(String contractId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();

        Contract contract = new Contract();
        contract.setContractId(contractId);
        // DB에 저장
        try {
            contract = contractRepository.saveAndFlush(contract);
            // 엔티티를 다시 로드 (증가된 EMA_BASE_NUMBER를 바로 읽어오지 못하는 문제가 있어서 수정함)
            entityManager.refresh(contract);
            contract = contractRepository.findById(contract.getContractId())
                    .orElseThrow(() -> new RuntimeException("Contract not found"));
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to INSERT DB");
        }
        return result;
    }

    @Transactional
    public ServiceResult<ContractMeta> setIssuedContractInput(String contractId, String emaId, String pcid,
            String oemId,
            Long memberKey, String memberGroupId, Long memberGroupSeq) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        // 단순 인풋만부터 저장
        Contract contract = target.get();
        contract.setIssued(1);
        contract.setEmaId(emaId);
        contract.setPcid(pcid);
        contract.setOemId(oemId);
        contract.setMemberKey(memberKey);
        contract.setMemberGroupId(memberGroupId);
        contract.setMemberGroupSeq(memberGroupSeq);

        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    public ServiceResult<ContractMeta> setIssuedContract(String contractId, String emaId, String pcid, String oemId,
            Long memberKey, String memberGroupId, Long memberGroupSeq, String contCert) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        // 단순 인풋값부터 저장
        Contract contract = target.get();
        contract.setIssued(1);
        contract.setEmaId(emaId);
        contract.setPcid(pcid);
        contract.setOemId(oemId);
        contract.setMemberKey(memberKey);
        contract.setMemberGroupId(memberGroupId);
        contract.setMemberGroupSeq(memberGroupSeq);
        contract.setFullCert(contCert);

        // 내용에서 Date관련 파싱
        try {
            System.out.println("임시체크 : \n" + contCert);
            String pem = conversionService.convertToPEM(contCert);
            CertificateInfo certInfo = conversionService.getCertInfoFromPEM(pem);

            System.out.println("성공");
            // String을 어떤형식으로 사용할 지 알 수가 없어서 일단 보류.

            String contractStartDate = DateTimeFormatHelper.formatToSimpleStyle(certInfo.getIssueDate());
            String contractEndDate = DateTimeFormatHelper.formatToSimpleStyle(certInfo.getExpirationDate());

            System.out.println(contractStartDate);
            contract.setContractStartDateString(contractStartDate);
            contract.setContractEndDateString(contractEndDate);

        } catch (Exception ex) {
            if (apiLogger.isErrorEnabled()) {
                apiLogger.error("TAG:CONTRACT_PARSE_ERROR,  MESSAGE: {}, Request: {}",
                        "인증서 분해 실패.", contCert);
            }
            result.fail(500, "Failed to parse PEM");
            ex.printStackTrace();
            return result;
        }
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    public ServiceResult<ContractMeta> issueFailed(String contractId, String resultMessage) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }
        Contract contract = target.get();
        contract.setIssued(-1);
        contract.setStatusMessage(resultMessage);
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    private ContractMeta convertToMeta(Contract contract) {
        if (contract == null)
            return null;
        ContractMeta meta = new ContractMeta();
        meta.setContractId(contract.getContractId());
        meta.setEmaBaseNumber(contract.getEmaBaseNumber());
        meta.setEmaId(contract.getEmaId());
        meta.setOemId(contract.getOemId());
        meta.setMemberKey(contract.getMemberKey());
        meta.setMemberGroupId(contract.getMemberGroupId());
        meta.setMemberGroupSeq(contract.getMemberGroupSeq());
        meta.setPcid(contract.getPcid());
        meta.setContractStartDtString(contract.getContractStartDateString());
        meta.setContractEndDtString(contract.getContractEndDateString());
        meta.setFullContCert(contract.getFullCert());

        return meta;
    }

    public ServiceResult<ContractMeta> setWhitelistedContract(String contractId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        Contract contract = target.get();
        contract.setWhitelisted(1);
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    public ServiceResult<ContractMeta> undoWhitelistedContract(String contractId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        Contract contract = target.get();
        contract.setWhitelisted(0);
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    public ServiceResult<ContractMeta> getContractByEmaId(String emaId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findByEmaId(emaId);
        } catch (Exception ex) {
            ex.printStackTrace();
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
        } else {
            result.succeed(convertToMeta(target.get()));
        }
        return result;
    }

    public ServiceResult<ContractMeta> revokeContractById(String contractId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        Contract contract = target.get();
        contract.setRevokedDate(new Date());
        contract.setStatus(2);
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        // ContractIdentity에서도 지워줌.
        // => 지우지말자. join해서 체크하던가 하고, 두자. (Status에서 체크할 수 있도록)
        try {
            List<ContractIdentity> relatedIdentities = contractIdRepository.findAllByContractId(contractId);
            if (relatedIdentities != null) {
                for (ContractIdentity identity : relatedIdentities) {
                    // identity.setContractId(null);
                }

                contractIdRepository.saveAllAndFlush(relatedIdentities);
            }
        } catch (Exception ex) {
            // 안되도 처리가 되긴함.
            ex.printStackTrace();
        }
        return result;
    }

    public ServiceResult<ContractStatus> getContractStatusByContractId(String contractId) {
        ServiceResult<ContractStatus> result = new ServiceResult<>();
        ContractStatus status = new ContractStatus();
        status.setStatus(-1);
        status.setMessage("Can't check contract by internal error");

        // 일단 EMA자체를 확인
        Optional<Contract> contract;
        try {
            contract = contractRepository.findById(contractId);
        } catch (Exception ex) {
            ex.printStackTrace();
            result.fail(500, "Failed to connect DB");
            return result;
        }

        // 일단 db에 없음
        if (contract.isEmpty()) {
            status.setMessage("존재하지 않는 id입니다.");
            result.succeed(status);
            return result;

        }
        Contract existContract = contract.get();

        Optional<ContractIdentity> contractIdentity = Optional.empty();
        try {
            List<ContractIdentity> contractIds = contractIdRepository
                    .findAllByContractId(existContract.getContractId());
            if (contractIds != null && !contractIds.isEmpty()) {
                contractIdentity = Optional.ofNullable(contractIds.get(0));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // 일단 working인지 체크.
        // 마지막에 될수도 있지만, 지금타임에 working이면 100% working이니까
        // 캐치가 안되는건 덮어씌워진 것이다.
        if (contractIdentity.isEmpty()) {
            status.setMessage("신규발급 요청등의 이유로 접근할 수 없는 계약입니다.");
            result.succeed(status);
            return result;
        }

        if (contractIdentity.get().getWorking() > 0 && contractIdentity.get().getWorked() == 0) {
            // 현재 작업이 진행중임.
            status.setMessage("현재 발급절차가 진행중입니다.");
            status.setStatus(-2);
            result.succeed(status);
            return result;
        }

        // 이제 도중이라는 전제없이 태그만으로 체크한다.
        int issued = existContract.getIssued();
        if (issued < 1) {
            status.setMessage("emaId 발급에 실패하였습니다. Log Message : " + existContract.getStatusMessage());
            result.succeed(status);
            return result;
        }

        if (existContract.getStatus() < 0) {
            status.setStatus(existContract.getStatus());
            status.setMessage("발급절차 진행 중 문제가 발생하였습니다. LogMessage : " + existContract.getStatusMessage());
            result.succeed(status);
            return result;
        } else if (existContract.getStatus() == 1) {
            status.setStatus(1);
            status.setMessage("만료된 인증서입니다.");
            result.succeed(status);
            return result;
        } else if (existContract.getStatus() == 2) {
            status.setStatus(2);
            status.setMessage("파기된 인증서입니다.");
            result.succeed(status);
            return result;
        }

        // push white-list 실패 시에도 정상으로 통일해서 응답하는 것으로 수정 24-07-11
        status.setStatus(0);
        status.setMessage("OK");
        status.setContractInfo(convertToMeta(existContract).buildContractInfo());
        result.succeed(status);
        return result;

        // 여긴 status가 정상이다.
        // if(existContract.getWhitelisted() < 1){
        //     status.setStatus(-1);
        //     status.setMessage("인증서는 유효할 수 있으나, Whitelist에서 제외되었습니다.");
        //     result.succeed(status);
        //     return result;
        // }else{
        //     // 이정도면 정상이다.
        //     status.setStatus(0);
        //     status.setMessage("OK");
        //     status.setContractInfo(convertToMeta(existContract).buildContractInfo());
        //     result.succeed(status);
        //     return result;
        // }
    }

    public ServiceResult<List<ContractMeta>> findActiveContractsByPcid(String pcid) {
        ServiceResult<List<ContractMeta>> result = new ServiceResult<>();

        List<Contract> contracts = new ArrayList<>();
        try {
            contracts = contractRepository.findAllByPcid(pcid);
        } catch (Exception ex) {

        }
        if (contracts.isEmpty()) {
            result.fail(404, "No Contract Found");
        } else {
            List<ContractMeta> contractMetas = new LinkedList<>();
            for (Contract contract : contracts) {
                // 살아있는거만 보낸다.
                if (isContractActive(contract)) {
                    contractMetas.add(convertToMeta(contract));
                }
            }
            result.succeed(contractMetas);
        }
        return result;
    }

    public ServiceResult<ContractMeta> findContractByMetaData(Long memberKey, String pcid, String oemId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();

        // 무조건 Identity에 있는 것을 기준으로 잡는다.
        String contractId = null;
        try {
            ContractIdentityUK idPair = new ContractIdentityUK(pcid, memberKey);
            contractId = contractIdRepository.findById(idPair).get().getContractId();
        } catch (Exception ex) {
            // 그냥 없는것으로 치고가도 무방하다.
        }

        if (contractId == null) {
            result.fail(404, "관련 인증서를 찾을 수 없습니다.");
        }

        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
        } else {
            // TODO 일단 oem의 일치여부는 무시한다. 나중에 oemId도 Identity의 UK조건이라면 전체적으로 갈아엎어야하니까.
            result.succeed(convertToMeta(target.get()));
        }
        return result;

    }

    public ServiceResult<ContractMeta> checkAuth(String contractId) {

        // throw new UnsupportedOperationException("Unimplemented method 'checkAuth'");

        // KEPCO가 죽었을 때 자체 검증 처리
        // 1. CRL과 대조해보기 -> 미리 다운로드해서 저장해놓은 CRL table에서, 조회할 ContarctCert의 시리얼넘버와 같은 것이
        // 있는지 확인한다.
        // (만약에 같은 것이 있다면 폐기된 인증서이므로 바로 검증 실패)

        // 2. white-list와 대조 -> KEPCO가 가진 white-list가 우리 DB의 인증서 상태 (0,1,2)와 항상 동기화
        // 되어있다는 전제가 필요하다.
        // (이 부분은 배치를 통한 동기화 로직에서 동일성 해결해야 함)
        // 이 경우, 우리 DB에서 해당 ContCert를 꺼내와서 인증서 상태 (0,1,2)를 보고 0이면 유효한 인증서로 검증해주면 된다.
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();

        try {
            target = contractRepository.findById(contractId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
            return result;
        }

        try {
            Contract contract = target.get();
            int status = contract.getStatus();
            if (status == 0) {
                result.succeed(convertToMeta(target.get()));
            } else {
                result.fail(500, "Contract is Revoked or Expired ");
            }
        } catch (Exception ex) {
            result.fail(500, "Failed to get Contract Status");
        }

        return result;
    }

}
