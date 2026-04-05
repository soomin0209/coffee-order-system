package com.example.coffeeordersystem.domain.payment.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.OrderExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.PaymentExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.PointExceptionEnum;
import com.example.coffeeordersystem.common.exception.domain.UserExceptionEnum;
import com.example.coffeeordersystem.domain.order.entity.Order;
import com.example.coffeeordersystem.domain.order.repository.OrderRepository;
import com.example.coffeeordersystem.domain.payment.consts.PaymentStatus;
import com.example.coffeeordersystem.domain.payment.dto.PaymentResponse;
import com.example.coffeeordersystem.domain.payment.entity.Payment;
import com.example.coffeeordersystem.domain.payment.repository.PaymentRepository;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.example.coffeeordersystem.common.config.redis.RedisKeyConstants.PAYMENT_COUNT_KEY;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final StringRedisTemplate redisTemplate;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PointRepository pointRepository;

    @Transactional
    public PaymentResponse executePayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ServiceErrorException(OrderExceptionEnum.ERR_ORDER_NOT_FOUND));

        if (paymentRepository.existsByOrderIdAndStatus(orderId, PaymentStatus.COMPLETED)) {
            throw new ServiceErrorException(PaymentExceptionEnum.ERR_PAYMENT_ALREADY_COMPLETED);
        }

        User user = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new ServiceErrorException(UserExceptionEnum.ERR_USER_NOT_FOUND));

        int pointBalance = pointRepository.sumAmountByUserID(user.getId());
        if (order.getTotalPrice() > pointBalance) {
            throw new ServiceErrorException(PointExceptionEnum.ERR_POINT_INSUFFICIENT);
        }

        String paymentNumber = createPaymentNumber();
        Payment payment = Payment.register(paymentNumber, user.getId(), order.getId(), order.getTotalPrice());
        paymentRepository.save(payment);

        Point point = Point.use(payment.getUserId(), payment.getOrderId(), -payment.getAmount());
        pointRepository.save(point);

        payment.complete();
        order.pay();

        return new PaymentResponse(payment.getId(), paymentNumber, payment.getAmount(), payment.getStatus());
    }

    private String createPaymentNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisKey = PAYMENT_COUNT_KEY + datePrefix;

        Long count = redisTemplate.opsForValue().increment(redisKey);

        if (count == null) {
            throw new ServiceErrorException(PaymentExceptionEnum.ERR_PAYMENT_CREATE_FAILED);
        }

        redisTemplate.expire(redisKey, Duration.ofDays(1));
        return String.format("PAY-%s-%08d", datePrefix, count);
    }
}
