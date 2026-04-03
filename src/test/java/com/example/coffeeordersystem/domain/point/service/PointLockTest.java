package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.point.dto.PointsChargeRequest;
import com.example.coffeeordersystem.domain.user.consts.UserRole;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@ActiveProfiles("test")
public class PointLockTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointChargeWithoutLockHelper helper;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동시 포인트 충전 - 락 없음")
    void chargePoints_withoutLock() throws InterruptedException {
        // given
        User user = userRepository.save(User.register("test", "test@test.com", "password", UserRole.USER));
        Long userId = user.getId();

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // when
        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    helper.charge(userId, 1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        // then
        User result = userRepository.findById(userId).orElseThrow();
        assertThat(result.getPointBalance()).isNotEqualTo(100000);
        System.out.printf("[락 없음] 소요 시간: %dms, 최종 포인트 잔액: %d%n", time, result.getPointBalance());
    }

    @Test
    @DisplayName("동시 포인트 충전 - 비관적락")
    void chargePoints_withPessimisticLock() throws InterruptedException {
        // given
        User user = userRepository.save(User.register("test", "test@test.com", "password", UserRole.USER));

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);

        // when
        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    pointService.chargePoints(new PointsChargeRequest(user.getId(), 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        long time = System.currentTimeMillis() - start;

        // then
        User result = userRepository.findById(user.getId()).orElseThrow();
        assertThat(result.getPointBalance()).isEqualTo(100000);
        System.out.printf("[락 적용] 소요 시간: %dms, 최종 포인트 잔액: %d%n", time, result.getPointBalance());
    }
}
