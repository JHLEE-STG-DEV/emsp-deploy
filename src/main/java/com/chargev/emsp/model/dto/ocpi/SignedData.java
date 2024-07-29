package com.chargev.emsp.model.dto.ocpi;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SignedData {
    @Schema(maxLength=36, description="SignedData 필드에 사용되는 인코딩의 이름입니다. 이것은 회사 또는 회사 그룹에서 인코딩에 부여한 이름입니다.")
    @JsonProperty("encoding_method")
    private String encodingMethod;
    @Schema(maxLength=36, description="[encoding_method]의 버전 입니다.")
    @JsonProperty("encoding_method_version")
    private Integer encodingMethodVersion;
    @Schema(maxLength=512, description="데이터 서명에 사용되는 공개 키(base64로 인코딩됨)입니다.")
    @JsonProperty("public_key")
    private String publicKey;
    @Schema(maxLength=512, description="하나 이상의 [SignedValue] 목록입니다.")
    @JsonProperty("signed_values")
    private SignedValues signedValues;
    @Schema(maxLength=512, description="EV 운전자에게 표시할 수 있는 URL입니다. 이 URL은 EV 운전자가 충전 [Session]에서 서명된 데이터를 확인할 수 있는 가능성을 제공합니다.")
    private String url;
}
