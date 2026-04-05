package com.example.coffeeordersystem.domain.point.repository;

import com.example.coffeeordersystem.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PointRepository extends JpaRepository<Point, Long> {
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Point p WHERE p.userId = :userId")
    int sumAmountByUserID(@Param("userId") Long userId);
}
