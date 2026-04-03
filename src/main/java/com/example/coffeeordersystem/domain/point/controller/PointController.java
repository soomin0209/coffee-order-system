package com.example.coffeeordersystem.domain.point.controller;

import com.example.coffeeordersystem.common.dto.BaseResponse;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeResponse;
import com.example.coffeeordersystem.domain.point.service.PointService;
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
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    @PostMapping
    public ResponseEntity<BaseResponse<PointsChargeResponse>> chargePoints(@Valid @RequestBody PointsChargeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.success(HttpStatus.CREATED.name(), "포인트 충전 성공", pointService.chargePoints(request)));
    }
}
