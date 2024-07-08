package com.chargev.emsp.model.dto.pnc;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractInfo {
    @Schema(description = "계약번호")
    private String emaid;
    @Schema(description = "계약시작일자")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String contractStartDt;
    @Schema(description = "계약종료일자")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private String contractEndDt;

    // 부가정보
    @Schema(description = "Pcid")
    private String pcid;
    @Schema(description = "oemId")
    private String oemId;
    @Schema(description = "memberKey")
    private Long memberKey;
    @Schema(description = "memberGroupId")
    private String memberGroupId;
    @Schema(description = "memberGroupSeq")
    private Long memberGroupSeq;
}

