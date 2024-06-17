package com.chargev.emsp.model.dto.pnc;

import java.util.List;
import lombok.Data;

@Data
public class KpipReqBodyPushWhitelist {
    private List<KpipReqBodyPushWhitelistItem> emaiList;
}
