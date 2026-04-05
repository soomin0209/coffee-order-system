package com.example.coffeeordersystem.domain.payment.producer;

import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import static com.example.coffeeordersystem.common.config.kafka.KafkaTopicConstants.TOPIC_PAYMENT_COMPLETED;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentCompletedEvent> paymentCompletedEventKafkaTemplate;

    public void send(PaymentCompletedEvent event) {
        paymentCompletedEventKafkaTemplate.send(TOPIC_PAYMENT_COMPLETED, event);
    }
}
