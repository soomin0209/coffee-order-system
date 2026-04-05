package com.example.coffeeordersystem.domain.payment.service;

import com.example.coffeeordersystem.domain.payment.entity.Payment;
import com.example.coffeeordersystem.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentFailService {

    private final PaymentRepository paymentRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failPayment(Payment payment) {
        payment.fail();
        paymentRepository.save(payment);
    }
}
