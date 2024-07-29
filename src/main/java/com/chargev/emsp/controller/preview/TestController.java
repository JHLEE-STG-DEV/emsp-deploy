package com.chargev.emsp.controller.preview;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.service.cryptography.CertificateConversionService;
import com.chargev.emsp.service.http.KpipApiService;
import com.chargev.emsp.service.preview.ContractManageService;
import com.chargev.emsp.service.preview.PreviewCertificationService;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@RestController
@Hidden
@Slf4j
@RequestMapping("{version}/test/module")
@Validated
@RequiredArgsConstructor
public class TestController {
    private final PreviewCertificationService sampleService;
    private final ContractManageService contractManageService;
    private final CertificateConversionService conversionService;
    private final KpipApiService kpipApiService;

    @PostMapping("/sample1")
    @Operation(summary = "내부점검용", description = """
            내부점검용
            """)
    public Object sample1(HttpServletRequest httpRequest,
            @RequestBody TestRequest request) {
        System.out.println(buildEmaId(1,1000266 ));

        return null;
    }
    
    @PostMapping("/sample2")
    @Operation(summary = "내부점검용", description = """
            내부점검용
            """)
    public Object sample2(HttpServletRequest httpRequest,
            @RequestBody TestRequest request) {
        String responseCrl = request.getParam1();
        
        conversionService.decodeCRL(responseCrl);
        return null;
    }
    private String buildEmaId(int oemCode, int baseNumber) {
        // New Rule :<EMAID> = <Country Code> <S> <Provider ID> <S> <eMA Instance> <S>
        // <Check Digit>
        // <S> 는 일단 없는것으로 하자. (제공해준 샘플에서 사용하지 않으므로)

        // baseNumber을 이용해 XXXXXXX(7자리) + D(1자리) 를 만들자.
        //
        int checkDigit = baseNumber % 10;
        int serial = baseNumber / 10;

        // Integer를 16진수 문자열로 변환
        String hexString = Integer.toHexString(serial);

        // 최소 7자리 형식으로 표현하기
        // 7자리를 다 차지했을때에 대한 제안은 받지 못했다. 그냥 시스템 정지이고 그때가서 해결한다 라는 포지션으로 이해한다. 이쪽에선 그래도 혹시모르니 자리수확장을 확보해놓는다.
        String formattedHexString;
        if (hexString.length() > 7) {
            formattedHexString = hexString; // 8자리 이상이면 그대로 사용
        } else {
            formattedHexString = String.format("%7s", hexString).replace(' ', '0'); // 7자리로 패드
        }

        return String.format("%s%s%d0%s%d", "KR", "CEV", oemCode, formattedHexString, checkDigit);
    }

    @Getter
    @Setter
    public static class TestRequest {
        private String param1;
        private String param2;
    }
}
