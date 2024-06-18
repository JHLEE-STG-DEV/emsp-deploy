package com.chargev.emsp.entity.authenticationentity;

import lombok.Data;

// 살아 있는 토큰을 의미한다. 이 값은 캐싱 간격을 두고 업데이트된다. 
@Data
public class LiveToken {
    private String subjectId;
    private int issueSerial;
    private int issueDate;
}
