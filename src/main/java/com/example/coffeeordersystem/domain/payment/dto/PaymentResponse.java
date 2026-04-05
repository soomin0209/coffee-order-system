package com.example.coffeeordersystem.domain.payment.dto;

import com.example.coffeeordersystem.domain.payment.consts.PaymentStatus;

public record PaymentResponse(
        Long id,
        String paymentNumber,
        int amount,
        PaymentStatus status
) {}
