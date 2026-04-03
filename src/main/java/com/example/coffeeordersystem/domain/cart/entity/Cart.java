package com.example.coffeeordersystem.domain.cart.entity;

import com.example.coffeeordersystem.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "carts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false)
    Long userId;

    @Column(nullable = false)
    Long menuId;

    @Column(nullable = false)
    int quantity;

    public static Cart register(
            Long userId,
            Long menuId
    ) {
        Cart cart = new Cart();

        cart.userId = userId;
        cart.menuId = menuId;
        cart.quantity = 1;

        return cart;
    }
}
