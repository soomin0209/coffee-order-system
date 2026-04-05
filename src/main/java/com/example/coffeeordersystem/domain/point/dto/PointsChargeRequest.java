package com.example.coffeeordersystem.domain.point.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PointsChargeRequest(
        @NotNull(message = "사용자 아이디는 필수입니다")
        Long userId,

        @Min(value = 1, message = "포인트 충전은 1P 이상부터 가능합니다")
        int amount
) {}
