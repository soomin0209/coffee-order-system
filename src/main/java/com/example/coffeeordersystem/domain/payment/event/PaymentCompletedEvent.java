package com.example.coffeeordersystem.domain.payment.event;

public record PaymentCompletedEvent(
        Long userId,
        Long menuId,
        int amount
) {}
