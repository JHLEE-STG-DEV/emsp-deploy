package com.chargev.emsp.controller.preview;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.service.preview.PreviewCertificationService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

//@Hidden
@RestController
@Slf4j
@RequestMapping("{version}/test/module")
@Validated
@RequiredArgsConstructor
public class TestController {
    private final PreviewCertificationService sampleService;
     // KEPCO가 사용하는 API
    @PostMapping("/sample1")
    @Operation(summary = "0. OEM 프로비저닝 변동으로 인한 계약 삭제", description = """
            OEM -> **ChargeLink -> eMSP** -> kafka <br><br>
            kafka : [MSG-EMSP-PNC-CONTRACT] 로 변경된 계약 정보 전송
            """)
    public Object sample1(HttpServletRequest httpRequest,
            @RequestBody TestRequest request) {

                Long ecKey = Long.parseLong(request.getParam1());
              return null;
    }

    @Getter
    @Setter
    public static class TestRequest{
        private String param1;
        private String param2;
    }
}
