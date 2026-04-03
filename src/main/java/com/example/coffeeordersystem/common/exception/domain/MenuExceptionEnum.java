package com.example.coffeeordersystem.common.exception.domain;

import com.example.coffeeordersystem.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MenuExceptionEnum implements ErrorCode {
    ERR_MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 메뉴입니다"),
    ERR_MENU_NOT_ON_SALE(HttpStatus.BAD_REQUEST, "판매 중이지 않은 메뉴입니다");

    private final HttpStatus httpStatus;
    private final String message;

    MenuExceptionEnum(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
