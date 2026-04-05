package com.example.coffeeordersystem.domain.payment.listener;

import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.example.coffeeordersystem.common.config.kafka.KafkaTopicConstants.TOPIC_PAYMENT_COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    @KafkaListener(
            topics = TOPIC_PAYMENT_COMPLETED,
            groupId = "payment-history-group",
            containerFactory = "paymentHistoryKafkaListenerContainerFactory"
    )
    public void consume(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트 수신 : userId={}, menuId={}, amount={}", event.userId(), event.menuId(), event.amount());
    }
}
