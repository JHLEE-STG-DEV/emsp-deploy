package com.chargev.emsp.service.contract;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.contract.Contract;
import com.chargev.emsp.model.dto.pnc.ContractMeta;
import com.chargev.emsp.repository.contract.ContractRepository;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.utils.IdHelper;
import com.chargev.utils.LocalFileManager;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ContractManageServiceImpl implements ContractManageService {
    private final ContractRepository contractRepository;
    private static final Logger logger = LoggerFactory.getLogger(ContractManageServiceImpl.class);

    private final CertificateConversionService conversionService;
    private boolean objectLogActive = true;
    private Path contractDirectoryPath;

    @PostConstruct
    public void init() {
        try {
            contractDirectoryPath = Paths.get("/var/log/chargeV/contracts");
            LocalFileManager.ensureDirectory(contractDirectoryPath);

        } catch (Exception e) {
            objectLogActive = false;
            logger.error(e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    private ContractMeta convertToMeta(Contract contract) {
        if (contract == null)
            return null;
        ContractMeta meta = new ContractMeta();
        meta.setContractId(contract.getContractId());
        meta.setEmaBaseNumber(contract.getEmaBaseNumber());
        meta.setEmaId(contract.getEmaId());
        meta.setContractStartDtString(contract.getContractStartDateString());
        meta.setContractEndDtString(contract.getContractEndDateString());
        return meta;

    }

    @Override
    public ServiceResult<ContractMeta> createEmptyContract() {
        ServiceResult<ContractMeta> result = new ServiceResult<>();

        Contract contract = new Contract();
        contract.setContractId(IdHelper.genLowerUUID32());
        // DB에 저장
        try {
            contract = contractRepository.saveAndFlush(contract);
            // 엔티티를 다시 로드 (증가된 EMA_BASE_NUMBER를 바로 읽어오지 못하는 문제가 있어서 수정함)
            contract = contractRepository.findById(contract.getContractId()).orElseThrow(() -> new RuntimeException("Contract not found"));
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to INSERT DB");
        }
        return result;
    }

    @Override
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
        contract.setEmaId(emaId);
        contract.setPcid(pcid);
        contract.setOemId(oemId);
        contract.setMemberKey(memberKey);
        contract.setMemberGroupId(memberGroupId);
        contract.setMemberGroupSeq(memberGroupSeq);
        contract.setFullCert(contCert);

        // 파일을 로컬에 저장.
        if (objectLogActive) {
            try {
                LocalFileManager.writeToFile(contCert, contractDirectoryPath.resolve(contractId));
            } catch (Exception ex) {

                // 실패
                ex.printStackTrace();
                logger.error("leafPem Object 저장 실패.");
            }
        }

        // 내용에서 Date관련 파싱
        //CertificateInfo certInfo = conversionService.getCertInfoFromPEM(contCert);

        // String을 어떤형식으로 사용할 지 알 수가 없어서 일단 보류.

        //String contractStartDate = certInfo.getIssueDate();
        //String contractEndDate = certInfo.getExpirationDate();
        //contract.setContractStartDateString(contractId);
        //contract.setContractEndDateString(contractEndDate);
        try {
            contract = contractRepository.saveAndFlush(contract);
            result.succeed(convertToMeta(contract));
        } catch (Exception ex) {
            result.fail(500, "Failed to update DB");
        }
        return result;
    }

    @Override
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

    @Override
    public ServiceResult<ContractMeta> undoWhitelistedContractg(String contractId) {
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

    @Override
    public ServiceResult<ContractMeta> getContractByEmaId(String emaId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findByEmaId(emaId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
        }else{
            result.succeed(convertToMeta(target.get()));
        }
        return result;
    }

    @Override
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
        return result;
    }

    @Override
    public ServiceResult<ContractMeta> findContractByMetaData(Long memberKey, String pcid, String oemId) {
        ServiceResult<ContractMeta> result = new ServiceResult<>();
        
        Optional<Contract> target = Optional.empty();
        try {
            target = contractRepository.findActiveByMemberKeyAndPcidAndOemId(memberKey, pcid, oemId);
        } catch (Exception ex) {
            result.fail(500, "Failed to query DB");
            return result;
        }
        if (target.isEmpty()) {
            result.fail(404, "No Contract Found");
        }else{
            result.succeed(convertToMeta(target.get()));
        }
        return result;

    }

    @Override
    public ServiceResult<String> getFullContCert(String contractId) {
        ServiceResult<String> result = new ServiceResult<>();
        if(!objectLogActive){
            result.fail(500, "서버에 내용저장이 활성화되지 않았습니다.");
            return result;
        }
        try {
           result.succeed(LocalFileManager.readFromFile(contractDirectoryPath.resolve(contractId)));
        } catch (Exception ex) {
result.fail(500, "Failed to read file.");
            // 실패
            ex.printStackTrace();
            logger.error("leafPem Object 저장 실패.");
        }
        return result;
    }


    @Override
    public ServiceResult<ContractMeta> checkAuth(String contractId) {

        // throw new UnsupportedOperationException("Unimplemented method 'checkAuth'");

        // KEPCO가 죽었을 때 자체 검증 처리
        // 1. CRL과 대조해보기 -> 미리 다운로드해서 저장해놓은 CRL table에서, 조회할 ContarctCert의 시리얼넘버와 같은 것이 있는지 확인한다.
        // (만약에 같은 것이 있다면 폐기된 인증서이므로 바로 검증 실패)

        // 2. white-list와 대조 -> KEPCO가 가진 white-list가 우리 DB의 인증서 상태 (0,1,2)와 항상 동기화 되어있다는 전제가 필요하다.
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
            if(status == 0) {
                result.succeed(convertToMeta(target.get()));
            } else {
                result.fail(500, "Contract is Revoked or Expired ");
            }
        } catch (Exception ex) {
            result.fail(500, "Failed to get Contract Status");
        }

        return result;
    }

    @Override
    public ServiceResult<ContractMeta> findContractByPcid(String pcid) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findContractByPcid'");
    }

}
