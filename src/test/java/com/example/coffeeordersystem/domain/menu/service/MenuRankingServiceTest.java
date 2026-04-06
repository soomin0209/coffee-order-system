package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.domain.menu.dto.MenuRankingResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.example.coffeeordersystem.common.config.redis.RedisKeyConstants.MENU_RANKING_DAILY_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MenuRankingServiceTest {

    @InjectMocks
    private MenuRankingService menuRankingService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @BeforeEach
    void setUp() {
        given(stringRedisTemplate.opsForZSet()).willReturn(zSetOperations);
    }

    @Test
    @DisplayName("메뉴 랭킹 점수 증가")
    void increaseMenuRanking_success() {
        // given
        LocalDate date = LocalDate.of(2026, 4, 6);
        String expectedKey = MENU_RANKING_DAILY_KEY + date;

        // when
        menuRankingService.increaseMenuRanking(1L, date);

        // then
        verify(zSetOperations).incrementScore(expectedKey, "1", 1);
    }

    @Test
    @DisplayName("인기 메뉴 TOP3 조회 성공")
    void findMenuRankingTop3InLast7Days_success() {
        // given
        ZSetOperations.TypedTuple<String> tuple1 = mockTuple("3", 150.0);
        ZSetOperations.TypedTuple<String> tuple2 = mockTuple("1", 120.0);
        ZSetOperations.TypedTuple<String> tuple3 = mockTuple("5", 90.0);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>();
        tuples.add(tuple1);
        tuples.add(tuple2);
        tuples.add(tuple3);

        given(zSetOperations.unionAndStore(anyString(), anyCollection(), anyString())).willReturn(10L);
        given(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(2L))).willReturn(tuples);

        // when
        List<MenuRankingResponse> result = menuRankingService.findMenuRankingTop3InLast7Days();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).menuId()).isEqualTo("3");
        assertThat(result.get(0).score()).isEqualTo(150.0);
        assertThat(result.get(1).menuId()).isEqualTo("1");
        assertThat(result.get(2).menuId()).isEqualTo("5");
    }

    @Test
    @DisplayName("인기 메뉴 TOP3 조회 - 데이터 없음 (null 반환)")
    void findMenuRankingTop3InLast7Days_nullResult() {
        // given
        given(zSetOperations.unionAndStore(anyString(), anyCollection(), anyString())).willReturn(0L);
        given(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(2L))).willReturn(null);

        // when
        List<MenuRankingResponse> result = menuRankingService.findMenuRankingTop3InLast7Days();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("인기 메뉴 TOP3 조회 - 데이터 없음 (빈 Set 반환)")
    void findMenuRankingTop3InLast7Days_emptyResult() {
        // given
        given(zSetOperations.unionAndStore(anyString(), anyCollection(), anyString())).willReturn(0L);
        given(zSetOperations.reverseRangeWithScores(anyString(), eq(0L), eq(2L))).willReturn(Set.of());

        // when
        List<MenuRankingResponse> result = menuRankingService.findMenuRankingTop3InLast7Days();

        // then
        assertThat(result).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private ZSetOperations.TypedTuple<String> mockTuple(String value, double score) {
        ZSetOperations.TypedTuple<String> tuple = mock(ZSetOperations.TypedTuple.class);
        given(tuple.getValue()).willReturn(value);
        given(tuple.getScore()).willReturn(score);
        return tuple;
    }
}