package com.example.coffeeordersystem.common.exception.domain;

import com.example.coffeeordersystem.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PaymentExceptionEnum implements ErrorCode {
    ERR_PAYMENT_ALREADY_COMPLETED(HttpStatus.CONFLICT, "이미 결제 완료된 주문입니다"),
    ERR_PAYMENT_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결제 번호 생성에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    PaymentExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
