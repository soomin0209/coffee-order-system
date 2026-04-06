package com.example.coffeeordersystem.domain.order.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.MenuExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.OrderExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.UserExceptionEnum;
import com.example.coffeeordersystem.domain.menu.consts.MenuStatus;
import com.example.coffeeordersystem.domain.menu.entity.Menu;
import com.example.coffeeordersystem.domain.menu.repository.MenuRepository;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateResponse;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.entity.OrderItem;
import com.example.coffeeordersystem.domain.order.repository.OrderItemRepository;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.coffeeordersystem.common.config.redis.RedisKeyConstants.ORDER_COUNT_KEY;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final StringRedisTemplate redisTemplate;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;

    @Transactional
    public OrderCreateResponse createOrder(OrderCreateRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ServiceErrorException(UserExceptionEnum.ERR_USER_NOT_FOUND));

        if (user.getDeletedAt() != null) {
            throw new ServiceErrorException(UserExceptionEnum.ERR_USER_DELETED);
        }

        Menu menu = menuRepository.findById(request.menuId())
                .orElseThrow(() -> new ServiceErrorException(MenuExceptionEnum.ERR_MENU_NOT_FOUND));

        if (menu.getStatus() != MenuStatus.ON_SALE) {
            throw new ServiceErrorException(MenuExceptionEnum.ERR_MENU_NOT_ON_SALE);
        }

        String orderNumber = createOrderNumber();
        int totalPrice = menu.getPrice() * request.quantity();
        Order order = Order.register(orderNumber, user.getId(), totalPrice);
        orderRepository.save(order);

        OrderItem orderItem = OrderItem.register(order.getId(), menu.getId(), menu.getName(), menu.getPrice(), request.quantity());
        orderItemRepository.save(orderItem);

        return new OrderCreateResponse(order.getId(), orderNumber, totalPrice, order.getStatus());
    }

    private String createOrderNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = ORDER_COUNT_KEY + datePrefix;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            throw new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_CREATE_FAILED);
        }

        redisTemplate.expire(redisKey, Duration.ofDays(1));
        return String.format("ORD-%s-%08d", datePrefix, count);
    }
}
