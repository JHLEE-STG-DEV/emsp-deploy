package com.chargev.emsp.service.oem;

import java.util.List;

import com.chargev.emsp.model.dto.oem.EmspRfidCard;
import com.chargev.emsp.model.dto.oem.EmspRfidCardIssuedDetail;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRegistration;
import com.chargev.emsp.model.dto.oem.EmspRfidCardRequest;
import com.chargev.emsp.service.ServiceResult;

public interface RfidService {
    ServiceResult<EmspRfidCardIssuedDetail> issueRfidCard(EmspRfidCardRequest request, String emspContractId, String trackId);
    ServiceResult<EmspRfidCard> registerRfid(String emspAccountKey, String emspContractId, EmspRfidCardRegistration request, String trackId);
    ServiceResult<EmspRfidCard> modifyRfidStatus(String emspAccountKey, String emspContractId, EmspRfidCard request, String trackId);
    ServiceResult<String> deleteRfidById(String emspAccountKey, String emspContractId, String cardId);
    public ServiceResult<List<EmspRfidCard>> getRfids(String emspAccountKey, String emspContractId, String trackId);
    public ServiceResult<EmspRfidCard> getRfid(String emspAccountKey, String emspContractId, String cardId, String trackId);
}
