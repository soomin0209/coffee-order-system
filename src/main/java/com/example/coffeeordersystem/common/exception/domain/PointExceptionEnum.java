package com.example.coffeeordersystem.common.exception.domain;

import com.example.coffeeordersystem.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PointExceptionEnum implements ErrorCode {
    ERR_POINT_INSUFFICIENT(HttpStatus.BAD_REQUEST, "포인트가 부족합니다");

    private final HttpStatus httpStatus;
    private final String message;

    PointExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
