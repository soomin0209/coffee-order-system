package com.example.coffeeordersystem.domain.point.dto;

import jakarta.validation.constraints.Min;

public record PointsChargeRequest(
        Long userId,
        @Min(value = 1, message = "포인트 충전은 1P 이상부터 가능합니다")
        int amount
) {}
