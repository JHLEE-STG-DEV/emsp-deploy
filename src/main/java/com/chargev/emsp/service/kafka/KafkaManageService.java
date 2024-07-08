package com.chargev.emsp.service.kafka;

import com.chargev.emsp.model.dto.pnc.EvseCertificate;
import com.chargev.emsp.model.dto.pnc.PncContract;
import com.chargev.emsp.service.ServiceResult;

public interface KafkaManageService {
    public ServiceResult<String> sendContCertSuccessKafka(String emaId, String oemId, String pcid, Long memberKey, String memberGroupId, Long memberGroupSeq, String contCert, String trackId, String type);
    public ServiceResult<String> sendContCertFailKafka(String emaId, String oemId, String pcid, Long memberKey, String memberGroupId, Long memberGroupSeq, String trackId, String message);


    public void sendCertResult(EvseCertificate kafkaObject, String trackId);
    public void sendSuspensionResult(PncContract kafkaObject, String trackId);
}
