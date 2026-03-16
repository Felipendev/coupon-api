package com.challenge.coupon.dto;

import com.challenge.coupon.domain.Coupon;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CouponDetailResponse (
    UUID id,
    String code,
    String description,
    BigDecimal discountValue,
    LocalDate expirationDate,
    boolean published) {

    public static CouponDetailResponse from(Coupon coupon) {
        return new CouponDetailResponse(
                coupon.getId(),
                coupon.getCode(),
                coupon.getDescription(),
                coupon.getDiscountValue(),
                coupon.getExpirationDate(),
                coupon.isPublished()
        );
    }
}