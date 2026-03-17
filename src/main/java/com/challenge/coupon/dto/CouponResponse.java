package com.challenge.coupon.dto;

import com.challenge.coupon.domain.Coupon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CouponResponse(UUID id, String code, String description, BigDecimal discountValue,
                             LocalDate expirationDate, boolean published) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.isPublished());
    }
}