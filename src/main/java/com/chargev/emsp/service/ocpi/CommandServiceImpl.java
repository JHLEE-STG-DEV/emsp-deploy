package com.chargev.emsp.service.ocpi;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.chargev.emsp.model.dto.ocpi.CpoCommandResponse;
import com.chargev.emsp.model.dto.ocpi.CpoReqBodyStartSession;
import com.chargev.emsp.service.ServiceResult;
import com.chargev.emsp.service.http.CpoApiService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {

    private final CpoApiService cpoApiService;

    // #region 
    @Override
    public ServiceResult<CpoCommandResponse> startSessionToCpo(CpoReqBodyStartSession request, String ecKey, String trackId) {
        ServiceResult<CpoCommandResponse> serviceResult = new ServiceResult<>();

        ServiceResult<Map<String, Object>> cpoResult = cpoApiService.startSessionToCpo(request, ecKey, trackId);

        if (cpoResult.isFail()) {
            serviceResult.fail(500, cpoResult.getErrorMessage());
        } else {
            CpoSessionFactory factory = new CpoSessionFactory(cpoResult.get());
            CpoCommandResponse cpoRes = new CpoCommandResponse();
            cpoRes.setResult(factory.getResult());
            cpoRes.setCode(factory.getCode());
            cpoRes.setMessage(factory.getMessage());
            cpoRes.setChargeNumber(factory.getChargeNumber());
            serviceResult.succeed(cpoRes);
        }
        return serviceResult;
    }

    private class CpoSessionFactory {
        private Map<String, Object> rawResult;

        private boolean systemStatus = true;
        @Getter
        private String result;
        @Getter
        private String code;
        @Getter
        private String message;
        @Getter
        private String chargeNumber;

        public CpoSessionFactory(Map<String, Object> rawResult) {
            this.rawResult = rawResult;
            try {
                result = (String) rawResult.get("result");
                code = (String) rawResult.get("code");
                message = (String) rawResult.get("message");
                chargeNumber = (String) rawResult.get("chargeNumber");
            } catch (Exception ex) {
                systemStatus = false;
                result = "INTERNAL_FAIL";
                message = "result 및 code 형식 불일치";
            }
        }
    }
    //#endregion

}
