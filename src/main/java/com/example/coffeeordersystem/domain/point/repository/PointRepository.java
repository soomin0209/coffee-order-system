package com.example.coffeeordersystem.domain.point.repository;

import com.example.coffeeordersystem.domain.point.entity.Point;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Point, Long> {
}
