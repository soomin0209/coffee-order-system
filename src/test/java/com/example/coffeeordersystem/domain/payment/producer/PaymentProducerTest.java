package com.example.coffeeordersystem.domain.payment.producer;

import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;

import static com.example.coffeeordersystem.common.config.kafka.KafkaTopicConstants.TOPIC_PAYMENT_COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentProducerTest {

    @InjectMocks
    private PaymentProducer paymentProducer;

    @Mock
    private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Test
    @DisplayName("결제 완료 이벤트 실시간 전송")
    void send_paymentCompletedEvent_success() {
        // given
        PaymentCompletedEvent event = new PaymentCompletedEvent(1L, 1L, 2000, LocalDateTime.now());

        // when
        paymentProducer.send(event);

        // then
        ArgumentCaptor<PaymentCompletedEvent> captor = ArgumentCaptor.forClass(PaymentCompletedEvent.class);
        verify(kafkaTemplate).send(eq(TOPIC_PAYMENT_COMPLETED), captor.capture());

        PaymentCompletedEvent captured = captor.getValue();
        assertThat(captured.userId()).isEqualTo(1L);
        assertThat(captured.menuId()).isEqualTo(1L);
        assertThat(captured.amount()).isEqualTo(2000);
    }
}