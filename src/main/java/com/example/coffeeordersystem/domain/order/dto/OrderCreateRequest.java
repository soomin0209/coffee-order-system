package com.example.coffeeordersystem.domain.order.dto;

import jakarta.validation.constraints.Min;

public record OrderCreateRequest(
        Long userId,
        Long menuId,
        @Min(value = 1, message = "1개 이상부터 주문 가능합니다")
        int quantity
) {}
