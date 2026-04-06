package com.example.coffeeordersystem.domain.payment.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.domain.order.consts.OrderStatus;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.payment.consts.PaymentStatus;
import com.example.coffeeordersystem.domain.payment.dto.PaymentResponse;
import com.example.coffeeordersystem.domain.payment.entity.Payment;
import com.example.coffeeordersystem.domain.payment.producer.PaymentProducer;
import com.example.coffeeordersystem.domain.payment.repository.PaymentRepository;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PointRepository pointRepository;

    @Mock
    private PaymentFailService paymentFailService;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentProducer paymentProducer;

    private void givenRedisCount(long count) {
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.increment(anyString())).willReturn(count);
    }

    private Order pendingOrder(Long id, Long userId, int totalPrice) {
        Order order = Order.register("ORD-TEST", userId, totalPrice);
        ReflectionTestUtils.setField(order, "id", id);
        return order;
    }

    private User activeUser(Long id, int pointBalance) {
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", id);
        user.updatePointBalance(pointBalance);
        return user;
    }

    @Test
    @DisplayName("결제 성공")
    void executePayment_success() {
        // given
        Order order = pendingOrder(1L, 1L, 2000);
        User user = activeUser(1L, 5000);
        OrderItem orderItem = OrderItem.register(1L, 10L, "아메리카노", 2000, 1);

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderIdAndStatus(1L, PaymentStatus.COMPLETED)).willReturn(false);
        given(userRepository.findByIdWithLock(1L)).willReturn(Optional.of(user));
        given(pointRepository.sumAmountByUserID(1L)).willReturn(5000);
        givenRedisCount(1L);
        given(paymentRepository.save(any(Payment.class))).willAnswer(i -> i.getArgument(0));
        given(pointRepository.save(any(Point.class))).willAnswer(i -> i.getArgument(0));
        given(orderItemRepository.findByOrderId(1L)).willReturn(Optional.of(orderItem));

        // when
        PaymentResponse response = paymentService.executePayment(1L);

        // then
        assertThat(response.amount()).isEqualTo(2000);
        assertThat(response.status()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(response.paymentNumber()).startsWith("PAY-");
        assertThat(user.getPointBalance()).isEqualTo(3000);
    }

    @Test
    @DisplayName("결제 실패 - 존재하지 않는 주문")
    void executePayment_failed_orderNotFound() {
        // given
        given(orderRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(999L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 주문입니다");
    }

    @Test
    @DisplayName("결제 실패 - 이미 결제된 주문")
    void executePayment_failed_alreadyPaid() {
        // given
        Order order = pendingOrder(1L, 1L, 5000);
        ReflectionTestUtils.setField(order, "status", OrderStatus.PAID);

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(1L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("결제 가능한 주문이 아닙니다");
    }

    @Test
    @DisplayName("결제 실패 - 중복 결제 시도")
    void executePayment_failed_duplicatePayment() {
        // given
        Order order = pendingOrder(1L, 1L, 5000);

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderIdAndStatus(1L, PaymentStatus.COMPLETED)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(1L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("이미 결제 완료된 주문입니다");
    }

    @Test
    @DisplayName("결제 실패 - 존재하지 않는 유저")
    void executePayment_failed_userNotFound() {
        // given
        Order order = pendingOrder(1L, 999L, 5000);

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderIdAndStatus(1L, PaymentStatus.COMPLETED)).willReturn(false);
        given(userRepository.findByIdWithLock(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(1L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }

    @Test
    @DisplayName("결제 실패 - 탈퇴한 유저")
    void executePayment_failed_deletedUser() {
        // given
        Order order = pendingOrder(1L, 1L, 5000);
        User user = activeUser(1L, 10000);
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderIdAndStatus(1L, PaymentStatus.COMPLETED)).willReturn(false);
        given(userRepository.findByIdWithLock(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(1L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("탈퇴한 사용자입니다");
    }

    @Test
    @DisplayName("결제 실패 - 포인트 부족")
    void executePayment_failed_insufficientPoints() {
        // given
        Order order = pendingOrder(1L, 1L, 10000);
        User user = activeUser(1L, 3000);

        given(orderRepository.findByIdWithLock(1L)).willReturn(Optional.of(order));
        given(paymentRepository.existsByOrderIdAndStatus(1L, PaymentStatus.COMPLETED)).willReturn(false);
        given(userRepository.findByIdWithLock(1L)).willReturn(Optional.of(user));
        given(pointRepository.sumAmountByUserID(1L)).willReturn(3000);

        // when & then
        assertThatThrownBy(() -> paymentService.executePayment(1L))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("포인트가 부족합니다");
    }
}
