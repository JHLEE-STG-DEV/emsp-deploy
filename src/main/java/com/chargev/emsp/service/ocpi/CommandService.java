package com.chargev.emsp.service.ocpi;

import com.chargev.emsp.model.dto.ocpi.CpoCommandResponse;
import com.chargev.emsp.model.dto.ocpi.CpoReqBodyStartSession;
import com.chargev.emsp.service.ServiceResult;

public interface CommandService {
    ServiceResult<CpoCommandResponse> startSessionToCpo(CpoReqBodyStartSession request, String ecKey, String trackId);
}
