package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyRevokeCert {
    // @Schema(required=true, description="certificateHashData")
    // private CertificateHashData certificateHashData;
    
    @Schema(required=true, description="Base64 인코딩된 PEM")
    private String certificate;
    @Schema(required=true, description="ecKey CPO에서 올라오는 충전기 고유 식별자")
    private Long ecKey;
    @Schema(description="요청 타입", defaultValue="DELETE", example="DELETE")
    private String issueType;
}
