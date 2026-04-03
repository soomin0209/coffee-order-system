package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.consts.UserRole;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @InjectMocks
    private PointService pointService;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("포인트 충전 성공")
    void chargePoints_success() {
        // given
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        PointsChargeRequest request = new PointsChargeRequest(1L, 10000);

        given(userRepository.findByIdWithLock(request.userId())).willReturn(Optional.of(user));
        given(pointRepository.save(any(Point.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointsChargeResponse response = pointService.chargePoints(request);

        // then
        assertThat(response.amount()).isEqualTo(10000);
        assertThat(response.pointBalance()).isEqualTo(10000);
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("포인트 충전 성공 - 누적")
    void chargePoint_success_accumulated() {
        // given
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        user.updatePointBalance(10000);
        PointsChargeRequest request = new PointsChargeRequest(1L, 10000);

        given(userRepository.findByIdWithLock(request.userId())).willReturn(Optional.of(user));
        given(pointRepository.save(any(Point.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        PointsChargeResponse response = pointService.chargePoints(request);

        // then
        assertThat(response.amount()).isEqualTo(10000);
        assertThat(response.pointBalance()).isEqualTo(20000);
        verify(pointRepository).save(any(Point.class));
    }

    @Test
    @DisplayName("포인트 충전 실패 - 존재하지 않는 유저")
    void chargePoints_failed() {
        // given
        PointsChargeRequest request = new PointsChargeRequest(999L, 10000);

        given(userRepository.findByIdWithLock(request.userId())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointService.chargePoints(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }
}