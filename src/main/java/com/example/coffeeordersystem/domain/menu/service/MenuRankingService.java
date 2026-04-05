package com.example.coffeeordersystem.domain.menu.service;

import com.example.coffeeordersystem.domain.menu.dto.MenuRankingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.example.coffeeordersystem.common.config.redis.RedisKeyConstants.MENU_RANKING_DAILY_KEY;
import static com.example.coffeeordersystem.common.config.redis.RedisKeyConstants.MENU_RANKING_TEMP_KEY;

@Service
@RequiredArgsConstructor
public class MenuRankingService {

    private final StringRedisTemplate stringRedisTemplate;

    public void increaseMenuRanking(Long menuId, LocalDate currentDate) {
        String key = MENU_RANKING_DAILY_KEY + currentDate;
        stringRedisTemplate.opsForZSet().incrementScore(key, String.valueOf(menuId), 1);
    }

    public List<MenuRankingResponse> findMenuRankingTop3InLast7Days() {
        LocalDate currentDate = LocalDate.now();
        List<String> keys = List.of(
                MENU_RANKING_DAILY_KEY + currentDate,
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(1),
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(2),
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(3),
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(4),
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(5),
                MENU_RANKING_DAILY_KEY + currentDate.minusDays(6)
        );

        String destKey = MENU_RANKING_TEMP_KEY + currentDate;

        stringRedisTemplate.opsForZSet().unionAndStore(
                keys.getFirst(),
                keys.subList(1, keys.size()),
                destKey
        );

        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(destKey, 0, 2);

        if (result == null || result.isEmpty()){
            return List.of();
        }

        return result.stream()
                .map(tuple -> new MenuRankingResponse(tuple.getValue(), tuple.getScore() != null ? tuple.getScore() : 0.0))
                .toList();
    }
}
