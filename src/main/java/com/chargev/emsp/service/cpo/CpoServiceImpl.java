package com.chargev.emsp.service.cpo;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.chargev.emsp.entity.oem.EmspContractEntity;
import com.chargev.emsp.model.dto.cpo.RfidVerify;
import com.chargev.emsp.model.dto.cpo.RfidVerifyResponse;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.oem.OemServiceUtils;
import com.chargev.emsp.model.dto.ocpi.CpoReqBodyStartSession;

import io.swagger.v3.oas.annotations.media.Schema;

import com.chargev.emsp.service.http.CpoApiService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CpoServiceImpl implements CpoService {
    private final OemServiceUtils oemServiceUtils;
    private final CpoApiService cpoApiService;

    @Override
    public ServiceResult<RfidVerifyResponse> validateRfid(RfidVerify request, String trackId) {

        ServiceResult<RfidVerifyResponse> result = new ServiceResult<>();

        try {
            // contract,rfid의 status가 모두 1인 것만 찾고 그 과정에서 없으면 IllegalArgumentException를 반환하므로 결과를 받아와서 분기를 따로 할 필요는 없다.
            // Exception이 발생하지 않았다면 유효한 계약과 rfid라는 의미이므로 바로 MPAY로 결제를 넘기면 된다.
            // TODO : ACCOUNT의 유효성도 검사해야 할까? Account가 유효하지 않고 Contract만 유효한 경우는 시스템 상 있을 수 없는데 더블체크가 필요할지 모르겠다.
            EmspContractEntity contract = oemServiceUtils.findContractByRfidNumAndRfidStatusAndContractStatus(request.getRfId(), 1, 1);
        } catch (IllegalArgumentException e) {
            result.fail(400, "결제 가능한 RFID가 아닙니다.");
            return result;
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
            return result;
        }

        // TODO : 이제 유효성 검증이 끝났으니 받아온 데이터를 가지고 MPAY로 결제를 넘기면 된다.
        // 아직 로직이 없으므로 성공 시에는 더미를 반환하는 것으로 하자.
        RfidVerifyResponse response = new RfidVerifyResponse();
        response.setTradeNumber(UUID.randomUUID().toString().replace("-", ""));
        response.setPaymentPrice(new BigDecimal("31000.00"));
        response.setEmspPrice(new BigDecimal("10000.00"));
        response.setConnectorType("");

        result.succeed(response);
        return result;
    }

    @Override
    public ServiceResult<String> validateContract(String contractId, String trackId) {

        ServiceResult<String> result = new ServiceResult<>();

        try {
            // contract,rfid의 status가 모두 1인 것만 찾고 그 과정에서 없으면 IllegalArgumentException를 반환하므로 결과를 받아와서 분기를 따로 할 필요는 없다.
            // Exception이 발생하지 않았다면 유효한 계약과 rfid라는 의미이므로 바로 MPAY로 결제를 넘기면 된다.
            // TODO : ACCOUNT의 유효성도 검사해야 할까? Account가 유효하지 않고 Contract만 유효한 경우는 시스템 상 있을 수 없는데 더블체크가 필요할지 모르겠다.
            EmspContractEntity contract = oemServiceUtils.findContractByContractIdAndContractStatusAndRfidStatus(contractId, 1, 1);
        } catch (IllegalArgumentException e) {
            result.fail(400, "유효한 계약을 찾을 수 없습니다.");
            return result;
        } catch (Exception e) {
            result.fail(500, "서버 에러가 발생했습니다.");
            return result;
        }

        // TODO : 이제 유효성 검증이 끝났으니 받아온 데이터를 가지고 MPAY로 결제를 넘기면 된다.

        // TODO : 결제까지 성공하면 이제 MSP로 충전 시작을 요청하면 된다.
        CpoReqBodyStartSession request = new CpoReqBodyStartSession();
        request.setOemCode("BENZ");
        request.setTradeNumber(UUID.randomUUID().toString().replace("-", ""));
        // request.setConnectorType("");
        request.setPaymentPrice(new BigDecimal("31000.00"));
        request.setEmspPrice(new BigDecimal("10000.00"));

        ServiceResult<Map<String, Object>> cpoResult = cpoApiService.startSessionToCpo(request, "1234567", trackId);

        // 아직 로직이 없으므로 성공여부 확인하지 않고 응답하자.

        result.succeed("OK");
        return result;
    }

}
