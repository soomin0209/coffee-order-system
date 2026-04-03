package com.example.coffeeordersystem.domain.point.entity;

import com.example.coffeeordersystem.domain.point.consts.PointType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "points")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    Long orderId;

    @Column(nullable = false)
    int amount;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    PointType type;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    LocalDateTime createdAt;

    public static Point charge(
            Long userId,
            int amount
    ) {
        Point point = new Point();

        point.userId = userId;
        point.amount = amount;
        point.type = PointType.CHARGE;

        return point;
    }

    public static Point use(
            Long userId,
            Long orderId,
            int amount
    ) {
        Point point = new Point();

        point.userId = userId;
        point.orderId = orderId;
        point.amount = amount;
        point.type = PointType.USE;

        return point;
    }
}
