package com.chargev.emsp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.pnc.KpipReqBodyEmaid;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyIssueContractCert;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyPncAuthorize;
import com.chargev.emsp.model.dto.pnc.KpipReqBodyVerifyContractCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyContractSuspension;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueCert;
import com.chargev.emsp.model.dto.pnc.PncReqBodyIssueContract;
import com.chargev.emsp.model.dto.response.KpipApiResponse;
import com.chargev.emsp.service.http.KpipApiService;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/pnc")
@Validated
@RequiredArgsConstructor
public class PncController {

    @Autowired
    private KpipApiService kpipApiService;

    // KEPCO가 사용하는 API
    @PostMapping("/provisioning/suspension")
    @Operation(summary = "0. OEM 프로비저닝 변동으로 인한 계약 삭제", description = "KPIP가 호출하는 api. OEM 프로비저닝 변경으로 인해 계약을 삭제(만료)한다.")
    public KpipApiResponse suspension(@RequestBody PncReqBodyContractSuspension request) {
        // OEM 프로비저닝 변경으로 생성된 기존 계약을 삭제(만료)처리해야 하는 상황을 KPIP가 알려줄 때 호출함.
        // 들어온 body의 pcid에 해당하는 계약을 삭제(만료) 시키고 그에 따른 응답을 반환한다.

        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

        return response;
    }

    @PostMapping("/cert/issuance")
    @Operation(summary = "1-1. EVSE 인증서 발급/갱신", description = "EVSE 인증서 발급/갱신 요청")
    public KpipApiResponse issueCert(@RequestBody PncReqBodyIssueCert request) {

        // 충전기 고유 식별자 => 실제 인증서 서명 요청할 때에는 사용되지 않음.
        // 서명된 인증서 관리 (매칭) 용도로 서버 내에서 사용해야 할 것으로 보임.
        String ecKey = (String) request.getEcKey();

        // 여기서부터는 KPIP에게 인증서 요청
        KpipReqBodyIssueCert req = new KpipReqBodyIssueCert();

        // EVSE / CSMS로 구분하나 현재 차지비에서 요청한 api는 EVSE용 only라서 이것으로 고정해놓음
        req.setCertType("EVSE");

        // csr 바로 토스하지 않고 제대로 된 csr인지 검증 필요할 것으로 보임
        // 주요 검증 내용: ECC 서명 알고리즘 사용하였는지, CN 제대로 들어있는지
        req.setCsr(request.getCertificateSigningRequest());

        // "N(New)" / "R(Reissue)"로 구분
        String type = (String) request.getType();
        String flag = type == "NEW" ? "N" : "R";
        req.setFlag(flag);

        String resultMsg = kpipApiService.issueCert(req);

        // 1. resultMsg Success인지 확인 (KPIP쪽에서 인증서 서명 처리 잘 되었는지)
        // 2. 서버 내부의 DB 등에 계약 관련 데이터 업데이트 처리
        // 3. KPIP에서 내려준 인증서(DER) 파일을 PEM으로 변환하여 내려보내야함
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/cert/revoke")
    @Operation(summary = "1-2. EVSE 인증서 삭제", description = "EVSE 인증서 삭제 요청 (미구현)")
    public KpipApiResponse revokeCert(@RequestBody PncReqBodyIssueCert request) {

        // 이부분은 아직 request body가 정리되지 않은 것 같아 미구현 상태임
        // 관련해서 질의해놓았음
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

        return response;
    }

    @PostMapping("/contract/issue")
    @Operation(summary = "2-1. PNC 계약", description = "BMW APP에서 PnC 가입")
    public KpipApiResponse issueContract(@RequestBody PncReqBodyIssueContract request) {

        // KPIP에서는 요청하지 않는 값인데 bmw에서 넣어주는 값임.
        // emaid 생성 후 멤버키와 매칭해서 저장하는 방식으로 관리하면 될 것 같음.
        String memberKey = (String) request.getMemberKey();

        // 계약번호(emaid) 생성
        // 원래 3가지 코드 합친 후 뒤에 7자리 난수 붙여야 하는데, 진짜 "랜덤생성" 할지 특정 로직을 부여할지 아직 모르는 상황이라
        // 우선은 난수 생성 기능을 따로 구현하지 않고 숫자 7자리 넣어놓았음
        String emaid = "KR" + "CEV" + "CA" + "0123456";

        // 여기서부터는 KPIP에게 계약 인증서 생성 요청
        KpipReqBodyIssueContractCert req = new KpipReqBodyIssueContractCert();

        // "New" "Update" "Add" 세가지 선택 가능한데, 신규에 대해서만 우선 작성하였음
        req.setReqType("New");
        // BMW에서 들어온 pcid 그대로 사용하면 될 것 같음 (차량 vin 번호)
        req.setPcid(request.getPcid());
        // 계약번호. 위에서 생성한 계약번호 사용
        req.setEmaid(emaid);
        // oemID는 KPIP에서 코드 부여해주었음. "BMW"로 고정하면 되지만
        // 어차피 BMW에서도 들어오기 때문에 들어온 값을 그대로 전달하기로 함
        req.setOemid(request.getOemid());
        // 유효기간인데 기본값인 24로 고정해놓는 것으로 작성함
        req.setExpPolicy("24");

        String resultMsg = kpipApiService.issueContractCert(req);

        // 1. resultMsg Success인지 확인 (KPIP쪽에서 계약인증서 잘 생성되었는지)
        // 2. 서버 내부의 DB 등에 계약 관련 데이터 업데이트 처리
        // 3. 가입 결과 CPO에게 kafka로 전달
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/contract/revoke")
    @Operation(summary = "2-2. PNC 계약 해지", description = "BMW APP에서 PnC 계약을 해지한다.")
    public KpipApiResponse revokeContract(@RequestBody KpipReqBodyEmaid request) {

        String resultMsg = kpipApiService.revokeContractCert(request);

        // 1. resultMsg Success인지 확인 (KPIP쪽에서 계약인증서 삭제 잘 수행되었는지)
        // 2. 서버 내부의 DB 등에 계약 관련 데이터 업데이트 처리
        // 3. 해지 결과 CPO에게 kafka로 전달
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg(resultMsg);

        return response;
    }

    @PostMapping("/authorize")
    @Operation(summary = "2-3. PNC 충전 인증", description = "CPO로부터 PnC 인증요청을 수신한다.")
    public KpipApiResponse pncAuthorize(@RequestBody KpipReqBodyPncAuthorize request) {

        // 1. PEM으로 들어온 인증서, 혹은 ecKey를 가지고 그에 매칭되는 emaid를 찾는다.
        // PnC account 체크
        KpipReqBodyEmaid req1 = new KpipReqBodyEmaid();
        req1.setEmaid("abc");
        String resultMsg1 = kpipApiService.authorizePnc(req1);

        // 2. PEM으로 들어온 인증서를 DER로 인코딩한다.
        // 계약인증서 유효성 확인 (들어온 PEM을 DER로 변경해서 contCert만 보내주면 됨)
        // 이건 필요한지 모르겠음 (위에것만 체크해도 되는건지)
        KpipReqBodyVerifyContractCert req2 = new KpipReqBodyVerifyContractCert();
        req2.setContCert("contCert");
        String resultMsg2 = kpipApiService.verifyContractCert(req2);

        // 결과로 emaid, bid 보내줘야 한다는데 bid 뭔지 확인 필요
        KpipApiResponse response = new KpipApiResponse();
        response.setResultCode("200");
        response.setResultMsg("Success");

        return response;
    }
}
