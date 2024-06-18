package com.chargev.emsp.model.dto.ocpi;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class Image {
    @Schema(maxLength=255, description = "이미지를 가져올 수 있는 URL 입니다.")
    private URL url;
    @Schema(maxLength=255, description = "썸네일 이미지를 가져올 수 있는 URL 입니다.")
    private URL thumbnail;
    @Schema(description = "사용자에게 올바른 사용법을 안내하기 위한 이미지 범주입니다. 올바른 사용법을 보장하기 위해서는 이미지 내용에 맞게 카테고리를 설정해야 합니다.")
    private ImageCategory category;
    @Schema(maxLength=4, description = "이미지 유형(gif, jpeg, png, svg 등) 입니다.")
    private String type;
    @Schema(maxLength=5, description = "전체 스케일 이미지의 너비입니다.")
    private Integer width;
    @Schema(maxLength=5, description = "전체 스케일 이미지의 높이입니다.")
    private Integer height;
}
