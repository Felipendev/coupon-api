package com.challenge.coupon.repository;

import com.challenge.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface CouponRepository extends JpaRepository<Coupon, UUID> {
    @Query(value = "SELECT COUNT(*) > 0 FROM coupon WHERE id = :id AND deleted = true", nativeQuery = true)
    boolean existsByIdAndDeletedTrue(UUID id);
}