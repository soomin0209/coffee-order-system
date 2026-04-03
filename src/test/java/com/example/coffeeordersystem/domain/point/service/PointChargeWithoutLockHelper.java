package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PointChargeWithoutLockHelper {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void charge(Long userId, int amount) {
        User user = userRepository.findById(userId).orElseThrow();
        user.updatePointBalance(amount);
    }
}

