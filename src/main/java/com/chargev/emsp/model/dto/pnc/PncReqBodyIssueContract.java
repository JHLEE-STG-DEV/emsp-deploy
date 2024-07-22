package com.chargev.emsp.model.dto.pnc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class PncReqBodyIssueContract {

    @Schema(description = "PCID", example = "KMKPNC123456789AB0")
    private String pcid;
    @Schema(description = "OEM ID", example = "KMK")
    private String oemId;
    @Schema(description = "Member Key")
    private Long memberKey;
    @Schema(description = "멤버 그룹 ID", example = "groupId")
    private String memberGroupId;
    @Schema(description = "멤버 그룹 seq", example = "012345")
    private Long memberGroupSeq;
    @Schema(description = "Issue Type (NEW / UPDATE)", example="NEW")
    private String issueType;
}
