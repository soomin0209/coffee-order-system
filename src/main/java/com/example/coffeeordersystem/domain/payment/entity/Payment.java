package com.example.coffeeordersystem.domain.payment.entity;

import com.example.coffeeordersystem.common.entity.BaseEntity;
import com.example.coffeeordersystem.domain.payment.consts.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = true)
    String paymentNumber;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    Long orderId;

    @Column(nullable = false)
    int amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    PaymentStatus status;

    public static Payment register(
            String paymentNumber,
            Long userId,
            Long orderId,
            int amount
    ) {
        Payment payment = new Payment();

        payment.paymentNumber = paymentNumber;
        payment.userId = userId;
        payment.orderId = orderId;
        payment.amount = amount;
        payment.status = PaymentStatus.PENDING;

        return payment;
    }

    public void complete() {
        this.status = PaymentStatus.COMPLETED;
    }
}
