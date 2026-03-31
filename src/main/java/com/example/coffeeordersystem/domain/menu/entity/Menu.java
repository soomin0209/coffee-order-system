package com.example.coffeeordersystem.domain.menu.entity;

import com.example.coffeeordersystem.common.entity.BaseEntity;
import com.example.coffeeordersystem.domain.menu.consts.MenuCategory;
import com.example.coffeeordersystem.domain.menu.consts.MenuStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "menus")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Menu extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    @Min(1)
    private int price;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MenuCategory category;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MenuStatus status;

    private LocalDateTime deletedAt;

    public static Menu register(
            String name,
            int price,
            MenuCategory category
    ) {
        Menu menu = new Menu();

        menu.name = name;
        menu.price = price;
        menu.category = category;
        menu.status = MenuStatus.ON_SALE;

        return menu;
    }
}
