package com.example.coffeeordersystem.domain.menu.listener;

import com.example.coffeeordersystem.domain.menu.service.MenuRankingService;
import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.example.coffeeordersystem.common.config.kafka.KafkaTopicConstants.TOPIC_PAYMENT_COMPLETED;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuRankingListener {

    private final MenuRankingService menuRankingService;

    @KafkaListener(
            topics = TOPIC_PAYMENT_COMPLETED,
            groupId = "menu-ranking-group",
            containerFactory = "menuRankingKafkaListenerContainerFactory"
    )
    public void consume(PaymentCompletedEvent event) {
        log.info("메뉴 랭킹 업데이트: menuId={}, paidAt={}", event.menuId(), event.paidAt());

        LocalDate currentDate = event.paidAt().toLocalDate();
        menuRankingService.increaseMenuRanking(event.menuId(), currentDate);
    }
}
