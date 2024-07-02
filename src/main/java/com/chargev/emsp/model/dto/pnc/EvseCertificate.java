package com.chargev.emsp.model.dto.pnc;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EvseCertificate {
    @Schema(required=true, description = "요청 시 수신한 ecKey")
    private Long ecKey;
    @Schema(required=true, description = "요청 시 수신한 authorities")
    private String authorities;
    @Schema(required=true, description = "요청 시 수신한 issueType")
    private String issueType;
    @Schema(required=true, description = "요청 시 수신한 certType")
    private String certType;
    @Schema(description = "발급된 certificateChain (발급 실패인 경우 null)")
    private String certificateChain;
    @Schema(description = "발급된 certificate (발급 실패인 경우 null)")
    private String certificate;
    @Schema(description = "발급된 expiredDate (발급 실패인 경우 null)")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String expiredDate;
    @Schema(description = "인증서 상태 NORMAL, EXPIRED, TERMINATION (발급 실패인 경우 null)")
    private CertStatus certificateStatus;
    @Schema(required=true, description = "발급 결과 SUCCESS, FAIL", defaultValue="SUCCESS", example="SUCCESS")
    private String result;
    @Schema(description = "발급 실패인 경우 실패 메시지")
    private String message;
}
