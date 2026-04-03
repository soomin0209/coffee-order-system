package com.example.coffeeordersystem.domain.payment.repository;

import com.example.coffeeordersystem.domain.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
