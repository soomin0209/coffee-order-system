package com.example.coffeeordersystem.domain.payment.event;

import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        Long userId,
        Long menuId,
        int amount,
        LocalDateTime paidAt
) {}
