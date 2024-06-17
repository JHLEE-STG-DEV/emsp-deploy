package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class SignedValues {
    @Schema(maxLength=32, required=true, description="값의 특성, 즉 이 값이 속한 이벤트입니다.")
    private String nature;
    @Schema(maxLength=512, required=true, description="인코딩되지 않은 데이터 문자열입니다. 콘텐츠의 형식은 [encoding_method]에 따라 달라집니다.")
    private String plain_data;
    @Schema(maxLength=5000, required=true, description="base64로 인코딩된 서명된 데이터의 Blob입니다. 내용의 형식은 [encoding_method]에 따라 달라집니다.")
    private String signed_data;
}
