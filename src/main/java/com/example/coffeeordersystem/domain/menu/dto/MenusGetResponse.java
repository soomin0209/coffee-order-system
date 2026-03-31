package com.example.coffeeordersystem.domain.menu.dto;

public record MenusGetResponse(
        Long id,
        String name,
        int price
) {}
