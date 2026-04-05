package com.example.coffeeordersystem.domain.payment.controller;

import com.example.coffeeordersystem.common.dto.BaseResponse;
import com.example.coffeeordersystem.domain.payment.dto.PaymentResponse;
import com.example.coffeeordersystem.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{orderId}")
    public ResponseEntity<BaseResponse<PaymentResponse>> executePayment(@PathVariable Long orderId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "결제 실행 성공", paymentService.executePayment(orderId)));
    }
}
