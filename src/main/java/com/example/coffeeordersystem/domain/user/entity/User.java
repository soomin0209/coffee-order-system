package com.example.coffeeordersystem.domain.user.entity;

import com.example.coffeeordersystem.common.entity.BaseEntity;
import com.example.coffeeordersystem.domain.user.consts.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(nullable = false, length = 50)
    String name;

    @Column(nullable = false, length = 100, unique = true)
    String email;

    @Column(nullable = false)
    String password;

    @Column(nullable = false)
    int pointBalance;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    UserRole role;

    LocalDateTime deletedAt;

    public static User register(
            String name,
            String email,
            String password,
            UserRole role
    ) {
        User user = new User();

        user.name = name;
        user.email = email;
        user.password = password;
        user.pointBalance = 0;
        user.role = role;

        return user;
    }

    public void updatePointBalance(int amount) {
        this.pointBalance += amount;
    }
}
