package com.example.coffeeordersystem.domain.point.service;

import com.example.coffeeordersystem.common.exception.ServiceErrorException;
import com.example.coffeeordersystem.common.exception.domain.UserExceptionEnum;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeRequest;
import com.example.coffeeordersystem.domain.point.dto.PointsChargeResponse;
import com.example.coffeeordersystem.domain.point.entity.Point;
import com.example.coffeeordersystem.domain.point.repository.PointRepository;
import com.example.coffeeordersystem.domain.user.entity.User;
import com.example.coffeeordersystem.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    @Transactional
    public PointsChargeResponse chargePoints(PointsChargeRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ServiceErrorException(UserExceptionEnum.ERR_USER_NOT_FOUND));

        Point point = Point.charge(request.userId(), request.amount());
        pointRepository.save(point);

        user.updatePointBalance(request.amount());

        return new PointsChargeResponse(
                point.getId(),
                user.getId(),
                point.getAmount(),
                user.getPointBalance()
        );
    }
}
