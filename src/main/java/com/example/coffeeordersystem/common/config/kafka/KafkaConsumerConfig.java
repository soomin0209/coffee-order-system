package com.example.coffeeordersystem.common.config.kafka;

import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootStrapServers;

    // 기본 Consumer 속성
    private Map<String, Object> baseConsumerProps(String groupId) {
        Map<String, Object> props = new HashMap<>();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        return props;
    }

    // 기본 ConsumerFactory
    private ConsumerFactory<String, PaymentCompletedEvent> buildConsumerFactory(String groupId) {
        JacksonJsonDeserializer<PaymentCompletedEvent> deserializer = new JacksonJsonDeserializer<>(PaymentCompletedEvent.class);
        deserializer.addTrustedPackages("com.example.kafkaredispractice.common.model.kafka.event");

        return new DefaultKafkaConsumerFactory<>(
                baseConsumerProps(groupId),
                new StringDeserializer(),
                deserializer
        );
    }


    // 결제 내역 전용 ConsumerFactory
    @Bean
    public ConsumerFactory<String, PaymentCompletedEvent> paymentHistoryConsumerFactory() {
        return buildConsumerFactory("payment-history-group");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> paymentHistoryKafkaListenerContainerFactory(
            CommonErrorHandler commonErrorHandlerWithDLT
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentCompletedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(paymentHistoryConsumerFactory());
        factory.setCommonErrorHandler(commonErrorHandlerWithDLT);

        return factory;
    }


    // ErrorHandler
    @Bean
    public CommonErrorHandler commonErrorHandlerWithDLT(KafkaTemplate<String, PaymentCompletedEvent> paymentCompletedEventKafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(paymentCompletedEventKafkaTemplate);

        FixedBackOff backOff = new FixedBackOff(1000L, 2L);

        return new DefaultErrorHandler(recoverer, backOff);
    }
}
