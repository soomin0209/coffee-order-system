package com.example.coffeeordersystem.domain.order.entity;

import com.example.coffeeordersystem.common.entity.BaseEntity;
import com.example.coffeeordersystem.domain.order.consts.OrderStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50, unique = true)
    String orderNumber;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    int totalPrice;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    OrderStatus status;

    private LocalDateTime deletedAt;

    public static Order register(
            String orderNumber,
            Long userId,
            int totalPrice
    ) {
        Order order = new Order();

        order.orderNumber = orderNumber;
        order.userId = userId;
        order.totalPrice = totalPrice;
        order.status = OrderStatus.PENDING;

        return order;
    }

    public void pay() {
        this.status = OrderStatus.PAID;
    }
}
