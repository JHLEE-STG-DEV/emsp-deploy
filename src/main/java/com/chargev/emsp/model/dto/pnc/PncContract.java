package com.chargev.emsp.model.dto.pnc;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncContract {
    @Schema(required=true, description="PNC계약 ID", defaultValue="KRCEVCA0123456", example="KRCEVCA0123456")
    private String emaId;
    @Schema(required=true, description="유저 특정이 가능한 key")
    private Long memberKey;
    @Schema(required=true)
    private String memberGroupId;
    @Schema(required=true)
    private Long memberGroupSeq;
    @Schema(required=true, description="BENZ, BMW", defaultValue="BMW", example="BMW")
    private String oemCode;
    @Schema(required=true, description="차대번호")
    private String vin;
    @Schema(required=true, description="인증서")
    private String certificate;
    @Schema(required=true, description="인증서 발급 기관 Enum (KEPCO, HUBJECT)", defaultValue="KEPCO", example="KEPCO")
    private Authorities authorities;
    @Schema(required=true, description="상태 (NORMAL, TERMINATION, EXPIRED)")
    private CertStatus status;
    @Schema(required = true, description = "만료일시 (ISO8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime expiredDate;
    @Schema(required = true, description = "요청일시 (ISO8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private ZonedDateTime requestDate;
}
