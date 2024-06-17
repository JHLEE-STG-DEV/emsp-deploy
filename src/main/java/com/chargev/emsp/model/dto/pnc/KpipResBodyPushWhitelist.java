package com.chargev.emsp.model.dto.pnc;

import java.util.List;
import lombok.Data;

@Data
public class KpipResBodyPushWhitelist {
    private List<KpipResBodyPushWhitelistItem> emaiList;
}
