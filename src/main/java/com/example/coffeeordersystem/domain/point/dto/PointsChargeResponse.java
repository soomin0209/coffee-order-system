package com.example.coffeeordersystem.domain.point.dto;

public record PointsChargeResponse(
        Long id,
        Long userId,
        int amount,
        int pointBalance
) {}
