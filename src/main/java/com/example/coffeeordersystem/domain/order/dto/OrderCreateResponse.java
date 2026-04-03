package com.example.coffeeordersystem.domain.order.dto;

import com.example.coffeeordersystem.domain.order.consts.OrderStatus;

public record OrderCreateResponse(
        String orderNumber,
        int totalPrice,
        OrderStatus status
) {}
