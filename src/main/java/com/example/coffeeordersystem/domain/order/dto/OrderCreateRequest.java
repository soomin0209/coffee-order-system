package com.example.coffeeordersystem.domain.order.dto;

public record OrderCreateRequest(
        Long userId,
        Long menuId,
        int quantity
) {}
