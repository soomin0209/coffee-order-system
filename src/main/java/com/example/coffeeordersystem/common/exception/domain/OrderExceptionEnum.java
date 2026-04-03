package com.example.coffeeordersystem.common.exception.domain;

import com.example.coffeeordersystem.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum OrderExceptionEnum implements ErrorCode {
    ERR_ORDER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "주문 번호 생성에 실패했습니다");

    private final HttpStatus httpStatus;
    private final String message;

    OrderExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
