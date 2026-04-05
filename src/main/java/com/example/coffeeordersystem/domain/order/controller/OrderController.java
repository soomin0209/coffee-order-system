package com.example.coffeeordersystem.domain.order.controller;

import com.example.coffeeordersystem.common.dto.BaseResponse;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateRequest;
import com.example.coffeeordersystem.domain.order.dto.OrderCreateResponse;
import com.example.coffeeordersystem.domain.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<BaseResponse<OrderCreateResponse>> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "주문 생성 성공", orderService.createOrder(request)));
    }
}
