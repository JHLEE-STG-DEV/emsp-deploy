package com.chargev.emsp.service.oem;

import com.chargev.emsp.service.ServiceResult;

public interface RfidService {
    ServiceResult<Void> deleteRfidById(String emspAccountKey, String emspContractId, String cardId);
}
