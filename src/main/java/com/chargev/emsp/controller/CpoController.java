package com.chargev.emsp.controller;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chargev.emsp.model.dto.cpo.ExcludeSettlement;
import com.chargev.emsp.model.dto.cpo.RfidVerify;
import com.chargev.emsp.model.dto.cpo.TransactionResponse;
import com.chargev.emsp.model.dto.response.PncApiResponse;
import com.chargev.emsp.model.dto.response.PncApiResponseObject;
import com.chargev.emsp.model.dto.response.PncResponseResult;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("{version}/cpo")
@Validated
@RequiredArgsConstructor
public class CpoController {

    @PostMapping("/rfid/validate")
    @Operation(summary = "eMSP RFID 인증", description = "eMSP에서 Account/Contract/RFID 유효성을 확인하고, 거래번호 및 금액 정보를 반환합니다.")
    public PncApiResponseObject verifyRfid(@RequestBody RfidVerify request) {
        PncApiResponseObject response = new PncApiResponseObject();
        // 1. request의 rfId와 연결된 계정, 계약, rfid의 유효성을 확인한다.
        // 2. 유효한 계약이면 거래번호, 결제금액, 단가를 보내준다.

        // 검증 성공
        response.setResult(PncResponseResult.SUCCESS);
        response.setCode("200");
        response.setMessage("Success");
        // 보내줘야 하는 데이터
        TransactionResponse data = new TransactionResponse();
        data.setTradeNumber("12345567");
        BigDecimal paymentPrice = new BigDecimal("30000.00");
        BigDecimal emspPrice = new BigDecimal("15000.00");
        data.setPaymentPrice(paymentPrice);
        data.setEmspPrice(emspPrice);
        response.setData(Optional.of(data));

        return response;
    }

    @PostMapping("/settlements/exclude")
    @Operation(summary = "정산 제외", description = "정산 중 결제 금액이 이상한 데이터를 보정하기 위해 특정 주문건에 대해 정산을 제외합니다.")
    public PncApiResponse excludeSettlement(@RequestBody ExcludeSettlement request) {
        PncApiResponse response = new PncApiResponse();
        // 1. request의 tradeNumber에 해당하는 주문 건을 정산 제외 처리한다.
        String tradeNumber = request.getTradeNumber();

        // 처리 성공
        response.setResult(PncResponseResult.SUCCESS);
        response.setCode("200");
        // 들어왔던 tradeNumber 메시지에 담아서 반환한다.
        response.setMessage(tradeNumber);

        return response;
    }

}
