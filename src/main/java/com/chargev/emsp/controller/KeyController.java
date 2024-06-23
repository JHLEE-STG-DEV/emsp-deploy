package com.chargev.emsp.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.entity.authenticationentity.TokenRequest;
import com.chargev.emsp.entity.keyentity.KeyRequest;
import com.chargev.emsp.model.dto.response.ApiResponseString;
import com.chargev.emsp.model.dto.response.OcpiResponseStatusCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@Slf4j
@RequestMapping("{version}/key")
@Validated
@RequiredArgsConstructor

public class KeyController {
    // 이 부분은 키 컨트롤러가 올바로 구현되어 있다는 전제 하에서 작성되며, 이 페이지에 있는 내용들은 키 서버의 설계에 따라서 변동될 수 있다.

    // TODO 전체 키 관리를 위한 처리를 해야 함 
    public String PRIVATE_KEY = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCBTog0rQv/cw4gHye5yavm7dbihyVtFeQjgMdXeZ70ZNg==";
    public String PUBLIC_KEY = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEJyawgnJcGIj+R39rhadCoUm8w0hWst+1nUCjaBatdLQnt4p+XStEVnYzr/zpmBeg5JF8wA4WyYi1HDY7Nl5Nzw==";

    @PostMapping("/request")
    public ApiResponseString generateToken(@RequestBody KeyRequest entity) {
        ApiResponseString response = new ApiResponseString();
        // TODO, 인증처리 IP 제한사항 처리해야 함 
        // TODO, 키 소스가 파일인지 DB인지 확인 필요. 키 주입 방식에 대한 결정 필요 
        if(entity == null) {
            response.setStatusCode(OcpiResponseStatusCode.INVALID_PARAMETER);
            response.setStatusMessage(OcpiResponseStatusCode.INVALID_PARAMETER.toString());
            return response;
        }

        return response;
    }
    
}
