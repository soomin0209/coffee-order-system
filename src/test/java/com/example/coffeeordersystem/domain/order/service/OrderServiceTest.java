package com.example.coffeeordersystem.domain.order.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.consts.MenuStatus;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.consts.OrderStatus;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
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
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuRepository menuRepository;

    private void givenRedisCount(long count) {
        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.increment(anyString())).willReturn(count);
    }

    @Test
    @DisplayName("주문 생성 성공")
    void createOrder_success() {
        // given
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        Menu menu = Menu.register("아메리카노", 2000, MenuCategory.COFFEE);
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 2);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));
        givenRedisCount(1L);
        given(orderRepository.save(any(Order.class))).willAnswer(i -> i.getArgument(0));
        given(orderItemRepository.save(any(OrderItem.class))).willAnswer(i -> i.getArgument(0));

        // when
        OrderCreateResponse response = orderService.createOrder(request);

        // then
        assertThat(response.totalPrice()).isEqualTo(4000);
        assertThat(response.status()).isEqualTo(OrderStatus.PENDING);
        assertThat(response.orderNumber()).startsWith("ORD-");
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 유저")
    void createOrder_failed_userNotFound() {
        // given
        OrderCreateRequest request = new OrderCreateRequest(999L, 1L, 1);
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 사용자입니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - 탈퇴한 유저")
    void createOrder_failed_deletedUser() {
        // given
        User user = User.register("deleted", "deleted@test.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "deletedAt", LocalDateTime.now());
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 1);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("탈퇴한 사용자입니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - 존재하지 않는 메뉴")
    void createOrder_failed_menuNotFound() {
        // given
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        OrderCreateRequest request = new OrderCreateRequest(1L, 999L, 1);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("존재하지 않는 메뉴입니다");
    }

    @Test
    @DisplayName("주문 생성 실패 - 판매 중이 아닌 메뉴")
    void createOrder_failed_menuNotOnSale() {
        // given
        User user = User.register("test", "test@test.com", "password", UserRole.USER);
        Menu menu = Menu.register("discontinued", 3000, MenuCategory.COFFEE);
        ReflectionTestUtils.setField(menu, "status", MenuStatus.DISCONTINUED);
        OrderCreateRequest request = new OrderCreateRequest(1L, 1L, 1);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(menuRepository.findById(1L)).willReturn(Optional.of(menu));

        // when & then
        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ServiceErrorException.class)
                .hasMessage("판매 중이지 않은 메뉴입니다");
    }
}