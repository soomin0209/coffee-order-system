package com.example.coffeeordersystem.domain.menu.listener;

import com.example.coffeeordersystem.domain.menu.service.MenuRankingService;
import com.example.coffeeordersystem.domain.payment.event.PaymentCompletedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuRankingListenerTest {

    @InjectMocks
    private MenuRankingListener menuRankingListener;

    @Mock
    private MenuRankingService menuRankingService;

    @Test
    @DisplayName("결제 완료 이벤트 수신 시 메뉴 랭킹 업데이트")
    void consume_updatesMenuRanking() {
        // given
        LocalDateTime paidAt = LocalDateTime.of(2026, 4, 6, 12, 0);
        PaymentCompletedEvent event = new PaymentCompletedEvent(1L, 10L, 5000, paidAt);

        // when
        menuRankingListener.consume(event);

        // then
        verify(menuRankingService).increaseMenuRanking(10L, LocalDate.of(2026, 4, 6));
    }

    @Test
    @DisplayName("결제 완료 이벤트 - 자정 경계 날짜 정확히 추출")
    void consume_extractsDateCorrectly() {
        // given
        LocalDateTime midnight = LocalDateTime.of(2026, 4, 7, 0, 0, 0);
        PaymentCompletedEvent event = new PaymentCompletedEvent(1L, 5L, 3000, midnight);

        // when
        menuRankingListener.consume(event);

        // then
        verify(menuRankingService).increaseMenuRanking(5L, LocalDate.of(2026, 4, 7));
    }
}