package com.example.coffeeordersystem.domain.order.consts;

public enum OrderStatus {
    PENDING,    // 결제 대기
    PAID,       // 결제 완료
    CONFIRMED,  // 주문 완료, 확인
    PREPARING,  // 제조중
    READY,      // 제조 완료, 픽업 대기
    COMPLETED   // 픽업 완료
}
