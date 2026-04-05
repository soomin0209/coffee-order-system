package com.example.coffeeordersystem.domain.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderCreateRequest(
        @NotNull(message = "사용자 아이디는 필수입니다")
        Long userId,

        @NotNull(message = "메뉴 아이디는 필수입니다")
        Long menuId,

        @Min(value = 1, message = "1개 이상부터 주문 가능합니다")
        int quantity
) {}
