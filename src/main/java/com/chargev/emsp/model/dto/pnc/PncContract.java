package com.chargev.emsp.model.dto.pnc;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncContract {
    @Schema(description="PNC계약 ID", defaultValue="KRCEVCA0123456", example="KRCEVCA0123456")
    private String emaId;
    @Schema(description="유저 특정이 가능한 key")
    private Long memberKey;
    @Schema(description="회원 그룹 ID")
    private String memberGroupId;
    @Schema(description="회원 그룹 SEQ", type="integer", format="int64", example="123456")
    private Long memberGroupSeq;
    @Schema(description="BENZ, BMW", defaultValue="BMW", example="BMW")
    private String oemCode;
    @Schema(description="차대번호")
    private String vin;
    @Schema(description="인증서")
    private String certificate;
    @Schema(description="인증서 발급 기관 Enum (KEPCO, HUBJECT)", defaultValue="KEPCO", example="KEPCO", type="Authorities")
    private Authorities authorities;
    @Schema(description="상태 (NORMAL, TERMINATION, EXPIRED)")
    private CertStatus status;
    @Schema(description = "만료일시 (ISO8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String expiredDate;
    @Schema(description = "요청일시 (ISO8601)")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String requestDate;
}
