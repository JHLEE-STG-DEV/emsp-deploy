package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyIssueContract {

    @Schema(description = "차량 VIN 번호", example = "KMKPNC123456789AB0")
    private String pcid;
    @Schema(description = "OEM ID", example = "BMW")
    private String oemid;
    @Schema(description = "Member Key", example = "testMeberId")
    private String memberKey;
    @Schema(description = "멤버 그룹 ID", example = "groupId")
    private String memberGroupId;
    @Schema(description = "멤버 그룹 seq", example = "012345")
    private Long memberGroupSeq;
}
