package com.example.coffeeordersystem.domain.order.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long orderId;

    @Column(nullable = false)
    Long menuId;

    @Column(nullable = false, length = 100)
    String name;

    @Column(nullable = false)
    int price;

    @Column(nullable = false)
    int quantity;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    public static OrderItem register(
            Long orderId,
            Long menuId,
            String name,
            int price,
            int quantity
    ) {
        OrderItem orderItem = new OrderItem();

        orderItem.orderId = orderId;
        orderItem.menuId = menuId;
        orderItem.name = name;
        orderItem.price = price;
        orderItem.quantity = quantity;

        return orderItem;
    }
}
